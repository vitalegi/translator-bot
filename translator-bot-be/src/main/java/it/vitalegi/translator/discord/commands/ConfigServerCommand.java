package it.vitalegi.translator.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import it.vitalegi.translator.discord.CommandHandler;
import it.vitalegi.translator.discord.DiscordBot;
import it.vitalegi.translator.discord.constants.DiscordPermission;
import it.vitalegi.translator.discord.service.DiscordPermissionService;
import it.vitalegi.translator.service.DiscordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ConfigServerCommand implements CommandHandler {

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals("config-server");
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);
        var serverId = e.getOptionAsString("server_id").orElseThrow(() -> new IllegalArgumentException("server_id is mandatory"));
        var monthlyMaxTotalCharacters = e.getOptionAsLong("month_max_tot_chars").orElse(100_000L);
        var monthlyMaxTotalCharactersPerUser = e.getOptionAsLong("month_max_tot_chars_x_user").orElse(50_000L);

        var userId = e.getUser().getId().asString();

        DiscordBot.executeBlocking(() -> discordService.updateDiscordServerLimits(serverId, monthlyMaxTotalCharacters, monthlyMaxTotalCharactersPerUser));
        log.info("user {}, config_server {}, max={}, per_user={}", userId, serverId, monthlyMaxTotalCharacters, monthlyMaxTotalCharactersPerUser);

        return e.reply("Successfully updated server");
    }
}
