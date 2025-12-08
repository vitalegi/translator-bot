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
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.Callable;

@Slf4j
@Service
@AllArgsConstructor
public class ChannelGroupUpdateCommand implements CommandHandler {

    public static final String CMD = "update-channel-group";

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals(CMD);
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);
        var channelGroupId = e.getOptionAsString("channel_group_id").orElseThrow(() -> new IllegalArgumentException("channel_group_id is mandatory"));
        var name = e.getOptionAsString("name").orElseThrow(() -> new IllegalArgumentException("name is mandatory"));
        var userId = e.getUser().getId().asString();

        var id = UUID.fromString(channelGroupId);
        executeBlocking(() -> discordService.updateChannelGroup(id, name)).block();
        log.info("user {}, update-channel-group {} {}", userId, id, name);

        return e.reply("Successfully updated server");
    }

    protected <T> Mono<T> executeBlocking(Callable<T> callable) {
        return Mono.fromCallable(callable) //
                .subscribeOn(DiscordBot.scheduler());
    }
}
