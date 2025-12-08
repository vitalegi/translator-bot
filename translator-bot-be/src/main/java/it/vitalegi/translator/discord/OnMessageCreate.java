package it.vitalegi.translator.discord;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import it.vitalegi.translator.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class OnMessageCreate {

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

        var channel = msg.getChannel().block();
        var channelId = getId(channel);

        //return msg.getChannel().flatMapMany(c -> c.createMessage(username + ": " + msg.getContent()));
        return getChannels(guild).zipWith(getUsername(msg)) //
                .flatMapMany((tuple) -> applyLogic(msg, guild, tuple.getT1(), channelId, tuple.getT2()));
    }

    protected Flux<Message> applyLogic(Message msg, Guild guild, List<GuildChannel> channels, String messageChannelId, String messageAuthor) {
        var messageChannelName = channels.stream().filter(c -> getId(c).equals(messageChannelId)).map(GuildChannel::getName).findFirst().orElse(null);
        log.info("Received a message on {} #{} from {}", guild.getName(), messageChannelName, messageAuthor);

        return Flux.fromIterable(channels) //
                .filter(channel -> !getId(channel).equals(messageChannelId)) //
                .flatMap(channel -> {
                    log.info("Send message to {}", channel.getName());
                    if (channel instanceof TextChannel textChannel) {
                        log.info("Channel is text channel, send");
                        return textChannel.createMessage(messageAuthor + ": " + msg.getContent());
                    } else {
                        log.info("Channel is: {}", channel.getClass());
                        return Mono.empty();
                    }
                });
    }

    protected Mono<List<GuildChannel>> getChannels(Guild guild) {
        return guild.getChannels().collectList();
    }

    protected DiscordChannel map(GuildChannel channel) {
        var out = new DiscordChannel();
        out.setId(getId(channel));
        out.setName(channel.getName());
        return out;
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
