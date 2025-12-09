package it.vitalegi.translator.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import it.vitalegi.translator.discord.CommandHandler;
import it.vitalegi.translator.discord.DiscordBot;
import it.vitalegi.translator.discord.constants.DiscordPermission;
import it.vitalegi.translator.discord.service.DiscordPermissionService;
import it.vitalegi.translator.service.DiscordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class InfoCommand implements CommandHandler {

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals("info");
    }

    @Override
    public Mono<Void> onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);

        return DiscordBot.executeBlocking(() -> discordService.getInfo()).flatMap(e::reply);
    }
}
