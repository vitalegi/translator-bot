package it.vitalegi.translator.discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import it.vitalegi.translator.discord.CommandHandler;
import it.vitalegi.translator.discord.DiscordBot;
import it.vitalegi.translator.discord.constants.DiscordPermission;
import it.vitalegi.translator.discord.service.DiscordPermissionService;
import it.vitalegi.translator.entity.DiscordServerWhitelistEntity;
import it.vitalegi.translator.service.DiscordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class EnableServerCommand implements CommandHandler {

    DiscordPermissionService discordPermissionService;
    DiscordService discordService;

    @Override
    public boolean accept(ChatInputInteractionEvent e) {
        return e.getCommandName().equals("enable-server") || e.getCommandName().equals("disable-server");
    }

    @Override
    public InteractionApplicationCommandCallbackReplyMono onEvent(ChatInputInteractionEvent e) {
        discordPermissionService.checkPermission(e, DiscordPermission.SUPERADMIN);
        var serverId = e.getOptionAsString("server_id").orElseThrow(() -> new IllegalArgumentException("server_id is mandatory"));
        boolean allowed = switch (e.getCommandName()) {
            case "enable-server" -> true;
            case "disable-server" -> false;
            default -> false;
        };
        var userId = e.getUser().getId().asString();

        executeBlocking(serverId, allowed).block();
        log.info("user {}, enable_server {}", userId, serverId);

        return e.reply("Successfully updated server");
    }

    protected Mono<DiscordServerWhitelistEntity> executeBlocking(String serverId, boolean allowed) {
        return Mono.fromCallable(() -> discordService.updateDiscordServerWhitelist(serverId, allowed)) //
                .subscribeOn(DiscordBot.scheduler());
    }

}
