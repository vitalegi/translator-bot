package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.ServerChannelGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServerChannelGroupRepository extends JpaRepository<ServerChannelGroupEntity, UUID> {
}
