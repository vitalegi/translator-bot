package it.vitalegi.translator.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;

public interface CommandHandler {

    boolean accept(ChatInputInteractionEvent e);

    InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e);
}
