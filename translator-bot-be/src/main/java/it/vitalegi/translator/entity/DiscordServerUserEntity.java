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
@Entity(name = "discord_server_user")
@Table(name = "discord_server_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordServerUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID discordServerUserId;
    @ManyToOne
    @JoinColumn(name = "discord_server_id", nullable = false)
    private DiscordServerEntity discordServer;
    private String userId;
    private String username;
    private Instant creationDate;
    private Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordServerUserEntity that = (DiscordServerUserEntity) o;
        return Objects.equals(discordServerUserId, that.discordServerUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordServerUserId);
    }

    @Override
    public String toString() {
        return "DiscordServerUserEntity{" + "discordServerUserId=" + discordServerUserId + ", userId='" + userId + '\'' + ", username='" + username + '\'' + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
