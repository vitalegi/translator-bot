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
@Entity(name = "discord_server_limits")
@Table(name = "discord_server_limits")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordServerLimitsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID discordServerLimitsId;
    @ManyToOne
    @JoinColumn(name = "discord_server_id", nullable = false)
    private DiscordServerEntity discordServer;
    private Integer monthlyMaxTotalCharacters;
    private Integer monthlyMaxTotalCharactersPerUser;
    private Instant creationDate;
    private Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordServerLimitsEntity that = (DiscordServerLimitsEntity) o;
        return Objects.equals(discordServerLimitsId, that.discordServerLimitsId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordServerLimitsId);
    }

    @Override
    public String toString() {
        return "DiscordServerLimitsEntity{" + "discordServerLimitsId=" + discordServerLimitsId + ", monthlyMaxTotalCharacters=" + monthlyMaxTotalCharacters + ", monthlyMaxTotalCharactersPerUser=" + monthlyMaxTotalCharactersPerUser + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
