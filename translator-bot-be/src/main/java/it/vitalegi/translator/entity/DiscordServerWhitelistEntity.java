package it.vitalegi.translator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@Entity(name = "discord_server_whitelist")
@Table(name = "discord_server_whitelist")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordServerWhitelistEntity {

    @Id
    private String discordServerId;
    private boolean allowed;
    private String description;
    private Instant creationDate;
    private Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordServerWhitelistEntity that = (DiscordServerWhitelistEntity) o;
        return Objects.equals(discordServerId, that.discordServerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordServerId);
    }

    @Override
    public String toString() {
        return "DiscordServerWhitelistEntity{" + "discordServerId='" + discordServerId + '\'' + ", allowed=" + allowed + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
