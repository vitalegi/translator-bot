package it.vitalegi.translator.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import it.vitalegi.translator.discord.CommandHandler;
import it.vitalegi.translator.discord.DiscordBotImpl;
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
public class AddServerCommand implements CommandHandler {

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals("add-server");
    }

    @Override
    public Mono<Void> onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);
        var serverId = e.getOptionAsString("server_id").orElseThrow(() -> new IllegalArgumentException("server_id is mandatory"));
        var name = e.getOptionAsString("name").orElseThrow(() -> new IllegalArgumentException("name is mandatory"));
        var userId = e.getUser().getId().asString();

        log.info("user {}, add_server {} {}", userId, serverId, name);

        return DiscordBotImpl.executeBlocking(() -> discordService.addServer(serverId, name)).flatMap(o -> e.reply("Successfully updated server"));
    }
}
