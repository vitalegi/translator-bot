package it.vitalegi.translator.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import reactor.core.publisher.Mono;

public interface CommandHandler {

    boolean accept(ChatInputInteractionEvent e);

    Mono<Void> onEvent(ChatInputInteractionEvent e);
}
