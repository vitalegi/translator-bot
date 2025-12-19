package it.vitalegi.translator.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import it.vitalegi.translator.entity.DiscordServerChannelLanguageEntity;
import it.vitalegi.translator.entity.DiscordServerUserEntity;
import it.vitalegi.translator.integration.aws.translate.AwsTranslateImpl;
import it.vitalegi.translator.service.DiscordService;
import it.vitalegi.translator.util.FileUtil;
import it.vitalegi.translator.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class OnMessageCreate {

    AwsTranslateImpl awsTranslate;
    DiscordService discordService;
    int maxMessageLength;
    Path tempDir;

    public OnMessageCreate(AwsTranslateImpl awsTranslate, DiscordService discordService, @Value("${discord.maxMessageLength}") int maxMessageLength, @Value("${discord.tmpDir}") Path tempDir) {
        this.awsTranslate = awsTranslate;
        this.discordService = discordService;
        this.maxMessageLength = maxMessageLength;
        this.tempDir = tempDir;
        FileUtil.createDirectories(tempDir);
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
                        .flatMap(valid -> Mono.zip(downloadAttachments(msg), getChannels(guild)) //
                                .map(tuple -> buildContext(msg, author.getT1(), author.getT2(), tuple.getT1(), guild, channelId, tuple.getT2())) //
                        ) //
                        .flatMapMany(this::processTranslations));
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

    protected TranslationMessageContext buildContext(Message msg, String authorUserId, String authorUsername, List<LocalAttachment> attachments, Guild guild, String originalChannelId, List<GuildChannel> discordGuildChannels) {
        var out = new TranslationMessageContext();
        out.setMessage(msg);
        out.setAuthorUserId(authorUserId);
        out.setAuthorUsername(authorUsername);
        out.setAttachments(attachments);
        out.setDiscordServerId(getId(guild));
        out.setMessageChannelId(originalChannelId);
        out.setDiscordChannels(discordGuildChannels);
        return out;
    }

    protected Flux<Message> processTranslations(TranslationMessageContext context) {
        return DiscordBotImpl.executeBlocking(() -> processTranslationsBlocking(context)) //
                .flatMapMany(Flux::fromIterable) //
                .flatMap(this::executeTranslation) //
                .flatMap(response -> sendMessage(context, response));
    }

    protected List<TranslationMessageRequest> processTranslationsBlocking(TranslationMessageContext context) {
        log.debug("Processing msg. messageChannelId={}, author={}/{}", context.getMessageChannelId(), context.getAuthorUserId(), context.getAuthorUsername());

        var discordServerId = context.getDiscordServerId();
        var user = discordService.syncDiscordServerUser(discordServerId, context.getAuthorUserId(), context.getAuthorUsername());
        log.debug("internal user={} <=> discord={} ({})", user.getDiscordServerUserId(), user.getUserId(), user.getUsername());

        var messageChannelName = context.getDiscordChannels().stream() //
                .filter(c -> getId(c).equals(context.getMessageChannelId())) //
                .map(GuildChannel::getName) //
                .findFirst().orElse(null);
        context.setMessageChannelName(messageChannelName);

        log.debug("DiscordServer={}, messageChannelName={}", discordServerId, messageChannelName);
        var sourceLanguage = discordService.getDiscordServerChannelLanguage(discordServerId, messageChannelName);
        if (sourceLanguage == null) {
            log.info("Channel {} (server {}) is not monitored, skip", messageChannelName, discordServerId);
            return Collections.emptyList();
        }
        var connectedEntries = discordService.getDiscordServerChannelGroupConnectedEntries(discordServerId, messageChannelName);

        return context.getDiscordChannels().stream() //
                .map(channel -> createTranslationMessageRequest(context, channel, sourceLanguage, user, connectedEntries)).filter(Objects::nonNull) //
                .toList();
    }

    protected TranslationMessageRequest createTranslationMessageRequest(TranslationMessageContext context, GuildChannel guildChannel, String sourceLanguage, DiscordServerUserEntity user, List<DiscordServerChannelLanguageEntity> connectedEntries) {
        if (getId(guildChannel).equals(context.getMessageChannelId())) {
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
        return new TranslationMessageRequest(context, entry.getServerChannelGroup().getServerChannelGroupId(), user.getDiscordServerUserId(), context.getMessage().getContent(), sourceLanguage, entry.getChannelSourceLanguage(), channel);
    }

    protected Mono<TranslationMessagePayload> executeTranslation(TranslationMessageRequest request) {
        return DiscordBotImpl.executeBlocking(() -> {
            var response = executeTranslationBlocking(request);
            return new TranslationMessagePayload(request.context(), request, response);
        });
    }

    protected Mono<Message> sendMessage(TranslationMessageContext context, TranslationMessagePayload response) {
        var request = response.request();
        var message = formatOutputMessage(request.context().getAuthorUsername(), request.sourceLanguage(), request.sourceMessage(), response.targetMessage());

        return addAttachments(MessageCreateSpec.builder().content(message), context) //
                .flatMap(builder -> request.targetChannel().createMessage(builder.build()));
    }

    protected Mono<List<LocalAttachment>> downloadAttachments(Message msg) {
        if (msg.getAttachments().isEmpty()) {
            return Mono.just(Collections.emptyList());
        }
        return Flux.fromIterable(msg.getAttachments()).flatMap(this::downloadAttachment).collectList();
    }

    protected Mono<LocalAttachment> downloadAttachment(Attachment attachment) {
        return DiscordBotImpl.executeBlocking(() -> {
            var start = System.currentTimeMillis();
            var url = attachment.getData().url();

            var file = tempFileRef(attachment);
            FileUtil.downloadFile(url, file.toFile());
            log.info("Downloaded {} ({} bytes) in {}, {}ms", attachment.getFilename(), attachment.getSize(), file, (System.currentTimeMillis() - start));
            return new LocalAttachment(attachment.getId().asLong(), attachment.getFilename(), file);
        });
    }

    protected Mono<MessageCreateSpec.Builder> addAttachments(MessageCreateSpec.Builder builder, TranslationMessageContext context) {
        if (context.getAttachments().isEmpty()) {
            return Mono.just(builder);
        }
        return DiscordBotImpl.executeBlocking(() -> addAttachmentsBlocking(builder, context.getAttachments()));
    }

    protected MessageCreateSpec.Builder addAttachmentsBlocking(MessageCreateSpec.Builder builder, List<LocalAttachment> attachments) {
        var start = System.currentTimeMillis();
        for (var attachment : attachments) {
            builder = addAttachmentBlocking(builder, attachment);
        }
        log.info("Attached files in {}ms", (System.currentTimeMillis() - start));
        return builder;
    }

    protected MessageCreateSpec.Builder addAttachmentBlocking(MessageCreateSpec.Builder builder, LocalAttachment attachment) {
        var start = System.currentTimeMillis();

        try {
            var fis = new FileInputStream(attachment.localRef().toFile());
            builder = builder.addFile(attachment.name(), fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Attached {} in {}ms", attachment.name(), (System.currentTimeMillis() - start));
        return builder;
    }

    protected Path tempFileRef(Attachment attachment) {
        return tempDir.resolve(UUID.randomUUID() + "_" + attachment.getFilename());
    }

    protected String formatOutputMessage(String author, String sourceLanguage, String sourceMessage, String targetMessage) {
        return "**" + author + "**: " + targetMessage;
    }

    protected String executeTranslationBlocking(TranslationMessageRequest request) {
        var ts = System.currentTimeMillis();

        var message = request.sourceMessage();
        if (message.length() > maxMessageLength) {
            message = message.substring(0, maxMessageLength);
        }

        var targetMessage = awsTranslate.translate(request.sourceLanguage(), request.targetLanguage(), message);
        var time = (System.currentTimeMillis() - ts);
        log.info("Translate message from {} (channel {}) to {} (channel {}) done in {}ms", request.sourceLanguage(), request.context().getMessageChannelName(), request.targetLanguage(), request.targetChannel().getName(), time);

        discordService.addDiscordServerUserMessage(request.sourceLanguage(), request.targetLanguage(), message.length(), targetMessage.length(), request.serverChannelGroupId(), request.context().getDiscordServerId(), request.discordServerUserId());

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
