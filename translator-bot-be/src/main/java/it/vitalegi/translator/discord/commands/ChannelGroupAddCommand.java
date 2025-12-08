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
public class ChannelGroupAddCommand implements CommandHandler {

    public static final String CMD = "add-channel-group";

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals(CMD);
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);

        var name = e.getOptionAsString("name").orElseThrow(() -> new IllegalArgumentException("name is mandatory"));
        var userId = e.getUser().getId().asString();

        DiscordBot.executeBlocking(() -> discordService.addChannelGroup(name)).block();
        log.info("user {}, add-channel-group {}", userId, name);

        return e.reply("Successfully updated server");
    }
}
