package it.vitalegi.translator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "discord_server_channel_language")
@Table(name = "discord_server_channel_language")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordServerChannelLanguageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID discordServerChannelLanguageId;
    @ManyToOne
    @JoinColumn(name = "server_channel_group_id", nullable = false)
    private ServerChannelGroupEntity serverChannelGroup;
    @ManyToOne
    @JoinColumn(name = "discord_server_id", nullable = false)
    private DiscordServerEntity discordServer;
    private String channelName;
    private String channelSourceLanguage;
    private Instant creationDate;
    private Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordServerChannelLanguageEntity that = (DiscordServerChannelLanguageEntity) o;
        return Objects.equals(discordServerChannelLanguageId, that.discordServerChannelLanguageId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordServerChannelLanguageId);
    }

    @Override
    public String toString() {
        return "DiscordServerChannelLanguageEntity{" + "discordServerChannelLanguageId=" + discordServerChannelLanguageId + ", channelName='" + channelName + '\'' + ", channelSourceLanguage='" + channelSourceLanguage + '\'' + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
