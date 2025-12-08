package it.vitalegi.translator.discord;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class DiscordBot {

    OnMessageCreate onMessageCreate;

    public DiscordBot(OnMessageCreate onMessageCreate, @Value("${DISCORD_TOKEN}") String token) {
        this.onMessageCreate = onMessageCreate;
        log.info("Create client");
        var client = DiscordClient.create(token);
        log.info("Client created");
        client.withGateway((gateway) -> {
            var readyHandler = gateway.on(ReadyEvent.class, e -> Mono.fromRunnable(() -> {
                var user = e.getSelf();
                log.info("User: {} - {}", user.getUsername(), user.getId());
            })).then();

            var onMessageCreateHandler = gateway.on(MessageCreateEvent.class, onMessageCreate::onEvent).then();

            return readyHandler.and(onMessageCreateHandler);
        }).block();
    }
}
