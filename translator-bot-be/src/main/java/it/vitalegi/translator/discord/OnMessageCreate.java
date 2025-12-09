package it.vitalegi.translator.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import it.vitalegi.translator.entity.MessageTranslatorEntity;
import it.vitalegi.translator.integration.aws.translate.AwsTranslateImpl;
import it.vitalegi.translator.repository.MessageTranslatorRepository;
import it.vitalegi.translator.service.DiscordService;
import it.vitalegi.translator.util.StringUtil;
import it.vitalegi.translator.util.Tuple2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class OnMessageCreate {

    AwsTranslateImpl awsTranslate;
    MessageTranslatorRepository messageTranslatorRepository;
    DiscordService discordService;

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
        return DiscordBot.executeBlocking(() -> precheckBlocking(discordServerId, serverName, discordUserId)).flatMap(allowed -> {
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
        log.info("Server quota of {} ({}). allowed: {}, actual: {}", discordServerId, discordServer.getName(), serverQuota, usedQuota);

        var userId = discordService.findDiscordServerUserId(discordServerId, discordUserId);
        if (userId != null) {
            var usedQuotaByUser = discordService.getServerUsedMonthlyQuotaByUser(discordServerId, userId);
            var serverQuotaByUser = discordServer.getMonthlyMaxTotalCharactersPerUser();
            if (usedQuotaByUser > serverQuotaByUser) {
                log.error("User {} in server {} ({}) used allowed quota. Allowed: {}, actual: {}", userId, discordServerId, discordServer.getName(), serverQuotaByUser, usedQuotaByUser);
                return false;
            }
            log.info("User server quota of {} - {} ({}). allowed: {}, actual: {}", userId, discordServerId, discordServer.getName(), serverQuotaByUser, usedQuotaByUser);
        }
        return true;
    }

    protected Flux<Message> processTranslations(Message msg, Guild guild, List<GuildChannel> guildChannels, String messageChannelId, String discordUserId, String discordUsername) {
        return DiscordBot.executeBlocking(() -> processTranslationsBlocking(msg, guild, guildChannels, messageChannelId, discordUserId, discordUsername)) //
                .flatMapMany(Flux::fromIterable);
    }

    protected List<Message> processTranslationsBlocking(Message msg, Guild guild, List<GuildChannel> guildChannels, String messageChannelId, String discordUserId, String discordUsername) {
        log.info("Processing msg. messageChannelId={}, author={}/{}", messageChannelId, discordUserId, discordUsername);

        var discordServerId = getId(guild);

        var user = discordService.syncDiscordServerUser(discordServerId, discordUserId, discordUsername);
        log.info("internal user={} <=> discord={} ({})", user.getDiscordServerUserId(), user.getUserId(), user.getUsername());

        var messageChannelName = guildChannels.stream().filter(c -> getId(c).equals(messageChannelId)).map(GuildChannel::getName).findFirst().orElse(null);
        log.info("DiscordServer={}, messageChannelName={}", discordServerId, messageChannelName);
        var sourceLanguage = discordService.getDiscordServerChannelLanguage(discordServerId, messageChannelName);
        if (sourceLanguage == null) {
            log.info("Channel {} (server {}) is not monitored, skip", messageChannelName, discordServerId);
            return Collections.emptyList();
        }
        var connectedEntries = discordService.getDiscordServerChannelGroupConnectedEntries(discordServerId, messageChannelName);

        return guildChannels.stream() //
                .filter(channel -> !getId(channel).equals(messageChannelId)) //
                .filter(channel -> channel instanceof TextChannel) //
                .map(channel -> (TextChannel) channel) //
                .map(channel -> {
                    log.info("Analyzing channel {}", channel.getName());
                    var entry = connectedEntries.stream().filter(connected -> connected.getChannelName().equals(channel.getName())).findFirst().orElse(null);
                    if (entry == null) {
                        log.info("Skip channel {}", channel.getName());
                        return null;
                    }
                    return new Tuple2<>(channel, entry);
                }) //
                .filter(Objects::nonNull) //
                .map(t -> {
                    var targetChannel = t.getT1();
                    var cfg = t.getT2();
                    var targetLanguage = cfg.getChannelSourceLanguage();
                    log.info("Translate message from {} (channel {}) to {} (channel {})", sourceLanguage, messageChannelName, targetLanguage, targetChannel.getName());
                    var translation = computeTranslationBlocking(cfg.getServerChannelGroup().getServerChannelGroupId(), discordServerId, user.getDiscordServerUserId(), msg.getContent(), sourceLanguage, targetLanguage);
                    return targetChannel.createMessage(formatOutputMessage(discordUsername, sourceLanguage, msg.getContent(), translation)).block();
                }) //
                .toList();
    }

    protected String formatOutputMessage(String author, String sourceLanguage, String sourceMessage, String targetMessage) {
        return "**" + author + "**: " + targetMessage + "\n--------------\n" + sourceLanguage + ": _" + sourceMessage + "_";
    }

    protected String computeTranslationBlocking(UUID serverChannelGroupId, String discordServerId, UUID authorId, String message, String sourceLanguage, String targetLanguage) {
        var ts = System.currentTimeMillis();
        // TODO move logic
        if (message.length() > 1000) {
            message = message.substring(0, 1000);
        }
        var targetMessage = awsTranslate.translate(sourceLanguage, targetLanguage, message);
        log.info("Translation done in {}ms", (System.currentTimeMillis() - ts));
        if (targetMessage.length() > 1000) {
            targetMessage = targetMessage.substring(0, 1000);
        }

        var entity = new MessageTranslatorEntity();
        entity.setSourceLanguage(sourceLanguage);
        entity.setTargetLanguage(targetLanguage);
        entity.setSourceMessage(message);
        entity.setTargetMessage(targetMessage);
        entity.setSourceLength(message.length());
        entity.setTargetLength(targetMessage.length());
        entity.setHits(1);
        entity.setCreationDate(Instant.now());
        entity.setLastUpdate(Instant.now());
        messageTranslatorRepository.save(entity);

        discordService.addDiscordServerUserMessage(sourceLanguage, targetLanguage, message.length(), targetMessage.length(), serverChannelGroupId, discordServerId, authorId);

        log.info("computeTranslationBlocking done in {}ms", (System.currentTimeMillis() - ts));
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
