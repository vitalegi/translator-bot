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
@Entity(name = "discord_server_user_message")
@Table(name = "discord_server_user_message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordServerUserMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID discordServerUserMessageId;
    @ManyToOne
    @JoinColumn(name = "server_channel_group_id", nullable = false)
    private ServerChannelGroupEntity serverChannelGroup;
    @ManyToOne
    @JoinColumn(name = "discord_server_id", nullable = false)
    private DiscordServerEntity discordServer;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private DiscordServerUserEntity discordServerUser;

    private String sourceLanguage;
    private String targetLanguage;
    private int sourceLength;
    private int targetLength;
    private Instant creationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordServerUserMessageEntity that = (DiscordServerUserMessageEntity) o;
        return Objects.equals(discordServerUserMessageId, that.discordServerUserMessageId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordServerUserMessageId);
    }

    @Override
    public String toString() {
        return "DiscordServerUserMessageEntity{" + "discordServerUserMessageId=" + discordServerUserMessageId + ", sourceLanguage='" + sourceLanguage + '\'' + ", targetLanguage='" + targetLanguage + '\'' + ", sourceLength=" + sourceLength + ", targetLength=" + targetLength + ", creationDate=" + creationDate + '}';
    }
}
