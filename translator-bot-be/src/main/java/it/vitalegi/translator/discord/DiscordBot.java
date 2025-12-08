package it.vitalegi.translator.discord;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class DiscordBot {

    OnMessageCreate onMessageCreate;

    OnChatInputInteraction onChatInputInteraction;

    public DiscordBot(OnMessageCreate onMessageCreate, OnChatInputInteraction onChatInputInteraction, @Value("${DISCORD_TOKEN}") String token) {
        this.onMessageCreate = onMessageCreate;
        log.info("Create client");
        var client = DiscordClient.create(token);
        log.info("Client created");

        client.withGateway((gateway) -> {
            new GlobalCommandsRegister(gateway.getRestClient()).registerCommands(List.of("discord-server.json", "whoami.json"));
            var readyHandler = gateway.on(ReadyEvent.class, e -> Mono.fromRunnable(() -> {
                var user = e.getSelf();
                log.info("User: {} - {}", user.getUsername(), user.getId());
            })).then();

            return readyHandler //
                    .and(gateway.on(MessageCreateEvent.class, onMessageCreate::onEvent).then()) //
                    .and(gateway.on(ChatInputInteractionEvent.class, onChatInputInteraction::onEvent).then());
        }).block();
    }
}
