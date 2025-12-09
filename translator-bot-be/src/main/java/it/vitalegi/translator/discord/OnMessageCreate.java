package it.vitalegi.translator.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import it.vitalegi.translator.entity.DiscordServerChannelLanguageEntity;
import it.vitalegi.translator.entity.DiscordServerUserEntity;
import it.vitalegi.translator.integration.aws.translate.AwsTranslateImpl;
import it.vitalegi.translator.service.DiscordService;
import it.vitalegi.translator.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OnMessageCreate {

    AwsTranslateImpl awsTranslate;
    DiscordService discordService;
    int maxMessageLength;

    public OnMessageCreate(AwsTranslateImpl awsTranslate, DiscordService discordService, @Value("${discord.maxMessageLength}") int maxMessageLength) {
        this.awsTranslate = awsTranslate;
        this.discordService = discordService;
        this.maxMessageLength = maxMessageLength;
    }

    public Flux<Message> onEvent(MessageCreateEvent e) {
        var msg = e.getMessage();
        if (msg.getAuthor().isEmpty() || msg.getAuthor().get().isBot()) {
            return Flux.empty();
        }
        return msg.getGuild().flatMapMany(guild -> onMessageCreate(msg, guild));
    }

    protected Flux<Message> onMessageCreate(Message msg, Guild guild) {
        if (guild == null) {
            log.error("Received null guild for {}", msg);
            return Flux.empty();
        }
        var discordServerId = getId(guild);
        var discordServerName = guild.getName();
        log.info("Server: {} - {}", discordServerId, discordServerName);

        var channel = msg.getChannel().block();
        var channelId = getId(channel);

        return getDiscordUserId(msg) //
                .zipWith(getUsername(msg)) //
                .flatMapMany(author -> precheck(discordServerId, discordServerName, author.getT1()) //
                        .flatMap(valid -> getChannels(guild)) //
                        .flatMapMany(channels -> processTranslations(msg, guild, channels, channelId, author.getT1(), author.getT2())));
    }

    protected Mono<Boolean> precheck(String discordServerId, String serverName, String discordUserId) {
        return DiscordBotImpl.executeBlocking(() -> precheckBlocking(discordServerId, serverName, discordUserId)).flatMap(allowed -> {
            if (allowed) {
                return Mono.just(true);
            }
            return Mono.empty();
        });
    }

    protected boolean precheckBlocking(String discordServerId, String serverName, String discordUserId) {
        if (!discordService.isServerAllowed(discordServerId)) {
            log.error("Server {} ({}) not allowed", discordServerId, serverName);
            return false;
        }
        var discordServer = discordService.getDiscordServer(discordServerId);
        var serverQuota = discordServer.getMonthlyMaxTotalCharacters();
        var usedQuota = discordService.getServerUsedMonthlyQuota(discordServerId);
        if (usedQuota > serverQuota) {
            log.error("Server {} ({}) used allowed quota. Allowed: {}, actual: {}", discordServerId, discordServer.getName(), serverQuota, usedQuota);
            return false;
        }
        log.debug("Server quota of {} ({}). allowed: {}, actual: {}", discordServerId, discordServer.getName(), serverQuota, usedQuota);

        var userId = discordService.findDiscordServerUserId(discordServerId, discordUserId);
        if (userId != null) {
            var usedQuotaByUser = discordService.getServerUsedMonthlyQuotaByUser(discordServerId, userId);
            var serverQuotaByUser = discordServer.getMonthlyMaxTotalCharactersPerUser();
            if (usedQuotaByUser > serverQuotaByUser) {
                log.error("User {} in server {} ({}) used allowed quota. Allowed: {}, actual: {}", userId, discordServerId, discordServer.getName(), serverQuotaByUser, usedQuotaByUser);
                return false;
            }
            log.debug("User server quota of {} - {} ({}). allowed: {}, actual: {}", userId, discordServerId, discordServer.getName(), serverQuotaByUser, usedQuotaByUser);
        }
        return true;
    }

    protected Flux<Message> processTranslations(Message msg, Guild guild, List<GuildChannel> guildChannels, String messageChannelId, String discordUserId, String discordUsername) {
        return DiscordBotImpl.executeBlocking(() -> processTranslationsBlocking(msg, guild, guildChannels, messageChannelId, discordUserId, discordUsername)) //
                .flatMapMany(Flux::fromIterable) //
                .flatMap(this::executeTranslation) //
                .flatMap(response -> {
                    var request = response.request();
                    var message = formatOutputMessage(request.discordUsername(), request.sourceLanguage(), request.sourceMessage(), response.targetMessage());
                    return request.targetChannel().createMessage(message);
                });
    }

    protected List<TranslationMessageRequest> processTranslationsBlocking(Message msg, Guild guild, List<GuildChannel> guildChannels, String messageChannelId, String discordUserId, String discordUsername) {
        log.debug("Processing msg. messageChannelId={}, author={}/{}", messageChannelId, discordUserId, discordUsername);

        var discordServerId = getId(guild);

        var user = discordService.syncDiscordServerUser(discordServerId, discordUserId, discordUsername);
        log.debug("internal user={} <=> discord={} ({})", user.getDiscordServerUserId(), user.getUserId(), user.getUsername());

        var messageChannelName = guildChannels.stream().filter(c -> getId(c).equals(messageChannelId)).map(GuildChannel::getName).findFirst().orElse(null);
        log.debug("DiscordServer={}, messageChannelName={}", discordServerId, messageChannelName);
        var sourceLanguage = discordService.getDiscordServerChannelLanguage(discordServerId, messageChannelName);
        if (sourceLanguage == null) {
            log.info("Channel {} (server {}) is not monitored, skip", messageChannelName, discordServerId);
            return Collections.emptyList();
        }
        var connectedEntries = discordService.getDiscordServerChannelGroupConnectedEntries(discordServerId, messageChannelName);

        return guildChannels.stream() //
                .map(channel -> createTranslationMessageRequest(channel, messageChannelId, messageChannelName, sourceLanguage, discordServerId, user, msg, connectedEntries)).filter(Objects::nonNull) //
                .toList();
    }

    protected TranslationMessageRequest createTranslationMessageRequest(GuildChannel guildChannel, String sourceChannelId, String sourceChannelName, String sourceLanguage, String discordServerId, DiscordServerUserEntity user, Message msg, List<DiscordServerChannelLanguageEntity> connectedEntries) {
        if (getId(guildChannel).equals(sourceChannelId)) {
            return null;
        }
        if (!(guildChannel instanceof TextChannel channel)) {
            return null;
        }
        log.debug("Analyzing channel {}", channel.getName());
        var entry = connectedEntries.stream().filter(connected -> connected.getChannelName().equals(channel.getName())).findFirst().orElse(null);
        if (entry == null) {
            log.debug("Skip channel {}", channel.getName());
            return null;
        }
        return new TranslationMessageRequest(entry.getServerChannelGroup().getServerChannelGroupId(), discordServerId, user.getDiscordServerUserId(), user.getUsername(), msg.getContent(), sourceLanguage, sourceChannelName, entry.getChannelSourceLanguage(), channel);

    }

    protected Mono<TranslationMessagePayload> executeTranslation(TranslationMessageRequest request) {
        return DiscordBotImpl.executeBlocking(() -> {
            var response = executeTranslationBlocking(request);
            return new TranslationMessagePayload(request, response);
        });
    }

    protected String formatOutputMessage(String author, String sourceLanguage, String sourceMessage, String targetMessage) {
        return "**" + author + "**: " + targetMessage + "\n--------------\n" + sourceLanguage + ": _" + sourceMessage + "_";
    }

    protected String executeTranslationBlocking(TranslationMessageRequest request) {
        var ts = System.currentTimeMillis();

        var message = request.sourceMessage();
        if (message.length() > maxMessageLength) {
            message = message.substring(0, maxMessageLength);
        }

        var targetMessage = awsTranslate.translate(request.sourceLanguage(), request.targetLanguage(), message);
        var time = (System.currentTimeMillis() - ts);
        log.info("Translate message from {} (channel {}) to {} (channel {}) done in {}ms", request.sourceLanguage(), request.sourceChannelName(), request.targetLanguage(), request.targetChannel().getName(), time);

        discordService.addDiscordServerUserMessage(request.sourceLanguage(), request.targetLanguage(), message.length(), targetMessage.length(), request.serverChannelGroupId(), request.discordServerId(), request.discordServerUserId());

        log.info("executeTranslationBlocking done in {}ms", (System.currentTimeMillis() - ts));
        return targetMessage;
    }

    protected Mono<List<GuildChannel>> getChannels(Guild guild) {
        return guild.getChannels().collectList();
    }

    protected Mono<String> getDiscordUserId(Message message) {
        return message.getAuthorAsMember().map(Member::getId).map(Snowflake::asString);
    }

    protected Mono<String> getUsername(Message message) {
        return message.getAuthorAsMember() //
                // server nickname
                .flatMap(this::getGuildUserNickname) //
                // global nickname
                .switchIfEmpty(getGlobalUserNickname(message));
    }

    protected Mono<String> getGuildUserNickname(Member member) {
        if (member.getMemberData().nick().isPresent()) {
            var nick = member.getMemberData().nick().get();
            if (nick.isPresent()) {
                var value = nick.get();
                if (StringUtil.isNotNullOrEmpty(value)) {
                    return Mono.just(nick.get());
                }
            }
        }
        return Mono.empty();
    }

    protected Mono<String> getGlobalUserNickname(Message message) {
        if (message.getAuthor().isEmpty()) {
            return Mono.empty();
        }
        var author = message.getAuthor().get();
        var nickname = author.getGlobalName();
        return nickname.map(Mono::just).orElseGet(() -> Mono.just("#unknown"));
    }

    protected String getId(Entity entity) {
        if (entity != null) {
            return entity.getId().asString();
        }
        return null;
    }
}
