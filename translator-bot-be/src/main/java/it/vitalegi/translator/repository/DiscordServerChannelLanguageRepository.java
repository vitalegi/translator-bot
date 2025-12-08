package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerChannelLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscordServerChannelLanguageRepository extends JpaRepository<DiscordServerChannelLanguageEntity, UUID> {

    @Query("""
            SELECT dscl
            FROM discord_server_channel_language dscl
            WHERE dscl.discordServer.discordServerId = :server_id
              AND dscl.serverChannelGroup.serverChannelGroupId = :server_channel_group_id
            """)
    List<DiscordServerChannelLanguageEntity> findAllByServerId(@Param("server_id") String serverId, @Param("server_channel_group_id") UUID serverChannelGroupId);
}
