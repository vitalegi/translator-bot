package it.vitalegi.translator.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import it.vitalegi.translator.discord.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WhoamiCommand implements CommandHandler {

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals("whoami");
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e) {
        var sb = new StringBuilder();
        sb.append("User id: ").append(e.getUser().getId().asString());
        return e.reply(sb.toString());
    }
}
