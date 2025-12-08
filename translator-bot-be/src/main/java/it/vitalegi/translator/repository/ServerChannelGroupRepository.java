package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.ServerChannelGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServerChannelGroupRepository extends JpaRepository<ServerChannelGroupEntity, UUID> {

    @Query("""
            SELECT scg
            FROM server_channel_group scg  JOIN scg.discordServerChannelLanguages dscl
            WHERE dscl.discordServer.discordServerId = :server_id
            """)
    List<ServerChannelGroupEntity> findAllByServerId(@Param("server_id") String serverId);

    ServerChannelGroupEntity findByName(String name);
}
