package it.vitalegi.translator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Entity(name = "server_channel_group")
@Table(name = "server_channel_group")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerChannelGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID serverChannelGroupId;
    private String name;
    private Instant creationDate;
    private Instant lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerChannelGroupEntity that = (ServerChannelGroupEntity) o;
        return Objects.equals(serverChannelGroupId, that.serverChannelGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serverChannelGroupId);
    }

    @Override
    public String toString() {
        return "ServerChannelGroupEntity{" + "serverChannelGroupId=" + serverChannelGroupId + ", name='" + name + '\'' + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + '}';
    }
}
