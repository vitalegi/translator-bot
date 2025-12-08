package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerChannelLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiscordServerChannelLanguageRepository extends JpaRepository<DiscordServerChannelLanguageEntity, UUID> {
}
