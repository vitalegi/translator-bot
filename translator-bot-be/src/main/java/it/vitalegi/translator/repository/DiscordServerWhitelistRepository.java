package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerWhitelistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface DiscordServerWhitelistRepository extends JpaRepository<DiscordServerWhitelistEntity, UUID> {

    DiscordServerWhitelistEntity findByDiscordServerId(String discordServerId);

    @Modifying
    @Query("update discord_server_whitelist dsw set dsw.allowed = false, dsw.lastUpdate = :now")
    void disableAllServers(@Param("now") Instant time);
}
