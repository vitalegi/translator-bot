package it.vitalegi.translator.discord.service;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import it.vitalegi.translator.configuration.DiscordAdminProperties;
import it.vitalegi.translator.discord.constants.DiscordPermission;
import it.vitalegi.translator.exception.UnauthorizedDiscordAccessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DiscordPermissionService {

    private DiscordAdminProperties properties;


    public void checkPermission(ChatInputInteractionEvent e, DiscordPermission permission) {
        if (hasPermission(e, permission)) {
            return;
        }
        throw new UnauthorizedDiscordAccessException("User " + getUserId(e) + " is not allowed to perform operation.");
    }

    public boolean hasPermission(ChatInputInteractionEvent e, DiscordPermission permission) {
        var userId = getUserId(e);
        var isSuperadmin = properties.getSuperAdminIds().stream().anyMatch(id -> id.equals(userId));
        return isSuperadmin;
    }

    protected String getUserId(ChatInputInteractionEvent e) {
        return e.getUser().getId().asString();
    }
}
