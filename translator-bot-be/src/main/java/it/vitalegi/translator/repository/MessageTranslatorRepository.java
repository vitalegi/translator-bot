package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.MessageTranslatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageTranslatorRepository extends JpaRepository<MessageTranslatorEntity, UUID> {

    @Query("SELECT SUM(mt.sourceLength) FROM message_translation mt")
    Long findSourcesLength();
}
