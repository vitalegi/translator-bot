package it.vitalegi.translator.discord;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

@Slf4j
public class DiscordBotImpl implements DiscordBot {

    private final OnMessageCreate onMessageCreate;
    private final OnChatInputInteraction onChatInputInteraction;
    private final String token;
    private final String commandsDir;

    public static Scheduler scheduler() {
        return Schedulers.boundedElastic();
    }


    public static <T> Mono<T> executeBlocking(Callable<T> callable) {
        return Mono.fromCallable(callable) //
                .subscribeOn(DiscordBotImpl.scheduler());
    }

    public DiscordBotImpl(OnMessageCreate onMessageCreate, OnChatInputInteraction onChatInputInteraction, @Value("${DISCORD_TOKEN}") String token, @Value("${discord.commandsDir}") String commandsDir) {
        this.onMessageCreate = onMessageCreate;
        this.onChatInputInteraction = onChatInputInteraction;
        this.token = token;
        this.commandsDir = commandsDir;
    }

    public void init() {
        log.info("Create client");
        var client = DiscordClient.create(token);
        log.info("Client created");

        client.withGateway((gateway) -> {
            new GlobalCommandsRegister(gateway.getRestClient(), commandsDir).registerCommands();
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
