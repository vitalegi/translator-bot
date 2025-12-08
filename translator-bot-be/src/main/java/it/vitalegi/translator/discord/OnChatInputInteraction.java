package it.vitalegi.translator.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import it.vitalegi.translator.exception.UnauthorizedDiscordAccessException;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OnChatInputInteraction {

    @Autowired
    List<CommandHandler> commandHandlers;

    public Publisher<?> onEvent(ChatInputInteractionEvent e) {
        var msg = e.getCommandName();
        log.info("Command: {}", e.getCommandName());
        var handler = commandHandlers.stream() //
                .filter(h -> h.accept(e)) //
                .findFirst();
        if (handler.isPresent()) {
            var h = handler.get();
            try {
                return h.onEvent(e);
            } catch (UnauthorizedDiscordAccessException ex) {
                return e.reply(ex.getMessage());
            } catch (Throwable ex) {
                e.reply(ex.getClass() + ": " + ex.getMessage());
            }
        }
        return e.reply("Unknown command");
    }
}
