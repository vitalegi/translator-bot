package it.vitalegi.translator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity(name = "discord_server")
@Table(name = "discord_server")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordServerEntity {

    @Id
    private String discordServerId;
    private String name;
    private Long monthlyMaxTotalCharacters;
    private Long monthlyMaxTotalCharactersPerUser;

    private Instant creationDate;
    private Instant lastUpdate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discordServer")
    private Set<DiscordServerUserEntity> users;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discordServer")
    private Set<DiscordServerChannelLanguageEntity> discordChannelLanguages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordServerEntity that = (DiscordServerEntity) o;
        return Objects.equals(discordServerId, that.discordServerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordServerId);
    }

    @Override
    public String toString() {
        return "DiscordServerEntity{" + "discordServerId=" + discordServerId + ", name='" + name + '\'' + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
