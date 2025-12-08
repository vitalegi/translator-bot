package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerWhitelistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiscordServerWhitelistRepository extends JpaRepository<DiscordServerWhitelistEntity, UUID> {

    DiscordServerWhitelistEntity findByDiscordServerId(String discordServerId);
}
