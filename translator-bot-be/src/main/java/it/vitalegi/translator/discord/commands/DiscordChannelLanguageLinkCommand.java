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
public class DiscordChannelLanguageLinkCommand implements CommandHandler {

    public static final String CMD = "discord-channel-language-link";

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals(CMD);
    }

    @Override
    public Mono<Void> onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);

        var channelGroupName = e.getOptionAsString("channel_group").orElseThrow(() -> new IllegalArgumentException("channel_group is mandatory"));
        var serverId = e.getOptionAsString("server_id").orElseThrow(() -> new IllegalArgumentException("server_id is mandatory"));
        var channel = e.getOptionAsString("channel").orElseThrow(() -> new IllegalArgumentException("channel is mandatory"));
        var language = e.getOptionAsString("language").orElseThrow(() -> new IllegalArgumentException("language is mandatory"));
        var userId = e.getUser().getId().asString();

        log.info("user {}, discord-channel-language-link {} {} {} {}", userId, channelGroupName, serverId, channel, language);

        return DiscordBotImpl.executeBlocking(() -> discordService.addDiscordServerChannelLanguage(channelGroupName, serverId, channel, language)) //
                .flatMap(o -> e.reply("Successfully updated server"));
    }

}
