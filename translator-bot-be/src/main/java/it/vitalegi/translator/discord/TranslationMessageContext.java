package it.vitalegi.translator.discord;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.Data;

import java.util.List;

@Data
public class TranslationMessageContext {
    Message message;
    String authorUserId;
    String authorUsername;
    List<LocalAttachment> attachments;
    String messageChannelId;
    String messageChannelName;
    String discordServerId;
    List<GuildChannel> discordChannels;
}
