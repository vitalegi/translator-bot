package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerUserMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
public interface DiscordServerUserMessageRepository extends JpaRepository<DiscordServerUserMessageEntity, UUID> {

    @Query("""
    SELECT SUM(dsum.sourceLength)
    FROM discord_server_user_message dsum
    WHERE dsum.creationDate >= :from_date
        AND dsum.discordServer.discordServerId = :server_id
    """)
    Long getTotalSourceLength(@Param("from_date") Instant fromDate, @Param("server_id") String serverId);

    @Query("""
    SELECT SUM(dsum.sourceLength)
    FROM discord_server_user_message dsum
    WHERE dsum.creationDate >= :from_date
        AND dsum.discordServer.discordServerId = :server_id
        AND dsum.discordServerUser.discordServerUserId = :discord_server_user_id
    """)
    Long getTotalSourceLength(@Param("from_date") Instant fromDate, @Param("server_id") String serverId, @Param("discord_server_user_id") UUID discordServerUserId);

    @Query("""
    SELECT COUNT(dsum.discordServerUserMessageId)
    FROM discord_server_user_message dsum
    WHERE dsum.creationDate >= :from_date
        AND dsum.discordServer.discordServerId = :server_id
    """)
    Long getTotalMessagesCount(@Param("from_date") Instant fromDate, @Param("server_id") String serverId);
}
