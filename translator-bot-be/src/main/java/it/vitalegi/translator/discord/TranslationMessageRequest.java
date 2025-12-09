package it.vitalegi.translator.discord;

import discord4j.core.object.entity.channel.TextChannel;

import java.util.UUID;

public record TranslationMessageRequest(UUID serverChannelGroupId,
                                        String discordServerId,
                                        UUID discordServerUserId,
                                        String discordUsername,
                                        String sourceMessage,
                                        String sourceLanguage,
                                        String sourceChannelName,
                                        String targetLanguage,
                                        TextChannel targetChannel

) {
}
