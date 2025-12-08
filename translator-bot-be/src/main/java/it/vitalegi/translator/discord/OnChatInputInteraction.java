package it.vitalegi.translator.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OnChatInputInteraction {

    @Autowired
    List<CommandHandler> commandHandlers;

    public InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e) {
        var msg = e.getCommandName();
        log.info("Command: {}", e);
        var handler = commandHandlers.stream() //
                .filter(h -> h.accept(e)) //
                .findFirst();
        if (handler.isPresent()) {
            return handler.get().onEvent(e);
        }
        return e.reply("Unknown command");
    }
}
