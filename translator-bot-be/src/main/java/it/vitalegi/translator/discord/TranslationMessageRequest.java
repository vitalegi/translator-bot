package it.vitalegi.translator.discord;

import discord4j.core.object.entity.channel.TextChannel;

import java.util.UUID;

public record TranslationMessageRequest(TranslationMessageContext context, //
                                        UUID serverChannelGroupId, UUID discordServerUserId, //
                                        String sourceMessage, String sourceLanguage, //
                                        String targetLanguage, TextChannel targetChannel) {
}
