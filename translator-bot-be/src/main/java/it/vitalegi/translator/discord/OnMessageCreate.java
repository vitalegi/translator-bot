package it.vitalegi.translator.discord;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import it.vitalegi.translator.configuration.TranslateConfig;
import it.vitalegi.translator.entity.MessageTranslatorEntity;
import it.vitalegi.translator.integration.aws.translate.AwsTranslateImpl;
import it.vitalegi.translator.repository.MessageTranslatorRepository;
import it.vitalegi.translator.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OnMessageCreate {

    TranslateConfig config;
    AwsTranslateImpl awsTranslate;
    MessageTranslatorRepository messageTranslatorRepository;

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
        var serverId = getId(guild);
        var serverName = guild.getName();
        log.info("Server: {} - {}", serverId, serverName);

        // TODO ignore message if server is not registered
        if (!serverId.equals(config.getAllowedServer())) {
            log.error("Server {} ({}) not allowed. Allowed: {}", serverId, serverName, config.getAllowedServer());
            return Flux.empty();
        }

        var channel = msg.getChannel().block();
        var channelId = getId(channel);

        // TODO ignore message if channel is not registered

        // TODO ignore message if server sent too many messages

        // TODO ignore message if user sent too many messages

        return getUsedQuota().flatMap(quota -> {
            if (quota > config.getMaxTotalCharacters()) {
                log.error("Server {} ({}) used allowed quota. Allowed: {}, actual: {}", serverId, serverName, config.getMaxTotalCharacters(), quota);
                return Mono.empty();
            }
            return Mono.just(quota);
        }).flatMapMany(quota -> //
                getChannels(guild).zipWith(getUsername(msg)) //
                        .flatMapMany((tuple) -> applyLogic(msg, guild, tuple.getT1(), channelId, tuple.getT2())));
    }

    protected Mono<Long> getUsedQuota() {
        return Mono.fromCallable(this::getUsedQuotaBlocking) //
                .subscribeOn(scheduler());
    }

    protected long getUsedQuotaBlocking() {
        var quota = messageTranslatorRepository.findSourcesLength();
        log.info("used quota: {}", quota);
        if (quota == null) {
            return 0L;
        }
        return quota;
    }

    protected Flux<Message> applyLogic(Message msg, Guild guild, List<GuildChannel> channels, String messageChannelId, String messageAuthor) {
        var messageChannelName = channels.stream().filter(c -> getId(c).equals(messageChannelId)).map(GuildChannel::getName).findFirst().orElse(null);
        var sourceLanguage = config.getChannels().get(messageChannelName);
        if (sourceLanguage == null) {
            log.info("Channel {} (server {}) is not monitored, skip", messageChannelName, getId(guild));
            return Flux.empty();
        }
        log.info("Received a message on {} #{} from {}", guild.getName(), messageChannelName, messageAuthor);

        return Flux.fromIterable(channels) //
                .filter(channel -> !getId(channel).equals(messageChannelId)) //
                .filter(channel -> channel instanceof TextChannel) //
                .map(channel -> (TextChannel) channel) //
                .flatMap(channel -> translateAndSend(msg, guild, channel, messageAuthor, sourceLanguage));
    }

    protected Mono<Message> translateAndSend(Message msg, Guild guild, TextChannel targetChannel, String messageAuthor, String sourceLanguage) {
        var targetLanguage = config.getChannels().get(targetChannel.getName());
        if (targetLanguage == null) {
            log.debug("Channel {} (server {}) is not a registered target, skip", targetChannel.getName(), getId(guild));
            return Mono.empty();
        }
        log.info("Send message to {}", targetChannel.getName());

        return computeTranslation(guild, messageAuthor, sourceLanguage, targetLanguage, msg.getContent()) //
                .flatMap(translatedMessage -> targetChannel.createMessage(formatOutputMessage(messageAuthor, sourceLanguage, msg.getContent(), translatedMessage)));
    }

    protected String formatOutputMessage(String author, String sourceLanguage, String sourceMessage, String targetMessage) {
        return "**" + author + "**: " + targetMessage + "\n--------------\n" + sourceLanguage + ": _" + sourceMessage + "_";
    }

    protected Mono<String> computeTranslation(Guild guild, String messageAuthor, String sourceLanguage, String targetLanguage, String message) {
        return Mono.fromCallable(() -> computeTranslationBlocking(guild, messageAuthor, sourceLanguage, targetLanguage, message)) //
                .subscribeOn(scheduler());
    }

    protected String computeTranslationBlocking(Guild guild, String messageAuthor, String sourceLanguage, String targetLanguage, String message) {
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
        log.info("computeTranslationBlocking done in {}ms", (System.currentTimeMillis() - ts));
        return targetMessage;
    }

    protected Mono<List<GuildChannel>> getChannels(Guild guild) {
        return guild.getChannels().collectList();
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

    protected Scheduler scheduler() {
        return Schedulers.boundedElastic();
    }
}
