package it.vitalegi.translator.discord;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
public class DiscordBot {

    OnMessageCreate onMessageCreate;

    OnChatInputInteraction onChatInputInteraction;

    public static Scheduler scheduler() {
        return Schedulers.boundedElastic();
    }

    public DiscordBot(OnMessageCreate onMessageCreate, OnChatInputInteraction onChatInputInteraction, @Value("${DISCORD_TOKEN}") String token) {
        this.onMessageCreate = onMessageCreate;
        log.info("Create client");
        var client = DiscordClient.create(token);
        log.info("Client created");

        client.withGateway((gateway) -> {
            new GlobalCommandsRegister(gateway.getRestClient()).registerCommands(List.of( //
                            "add-server.json", //
                            "enable-server.json", //
                            "disable-server.json", //
                            "config-server.json", //
                            "add-channel-group.json", //
                            "update-channel-group.json", //
                            "whoami.json", //
                            "info.json" //
                    ) //
            );
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
