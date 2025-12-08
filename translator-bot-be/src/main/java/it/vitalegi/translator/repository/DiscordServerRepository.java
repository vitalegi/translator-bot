package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordServerRepository extends JpaRepository<DiscordServerEntity, String> {
}
