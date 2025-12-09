package it.vitalegi.translator.repository;

import it.vitalegi.translator.entity.DiscordServerUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiscordServerUserRepository extends JpaRepository<DiscordServerUserEntity, UUID> {

    @Query("""
            SELECT dsu
            FROM discord_server_user dsu
            WHERE dsu.discordServer.discordServerId = :server_id
                AND dsu.userId = :user_id
            """)
    DiscordServerUserEntity findByDiscordServerIdAndUserId(@Param("server_id") String serverId, @Param("user_id") String userId);
}
