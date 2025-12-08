package it.vitalegi.translator.service;

import it.vitalegi.translator.entity.DiscordServerEntity;
import it.vitalegi.translator.entity.DiscordServerWhitelistEntity;
import it.vitalegi.translator.repository.DiscordServerChannelLanguageRepository;
import it.vitalegi.translator.repository.DiscordServerRepository;
import it.vitalegi.translator.repository.DiscordServerUserMessageRepository;
import it.vitalegi.translator.repository.DiscordServerUserRepository;
import it.vitalegi.translator.repository.DiscordServerWhitelistRepository;
import it.vitalegi.translator.repository.ServerChannelGroupRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;

@Slf4j
@Service
@AllArgsConstructor
public class DiscordService {
    DiscordServerChannelLanguageRepository discordServerChannelLanguageRepository;
    DiscordServerRepository discordServerRepository;
    DiscordServerUserMessageRepository discordServerUserMessageRepository;
    DiscordServerUserRepository discordServerUserRepository;
    DiscordServerWhitelistRepository discordServerWhitelistRepository;
    ServerChannelGroupRepository serverChannelGroupRepository;

    @Transactional
    public String addServer(String serverId, String name) {
        addDiscordServer(serverId, name, 100_000L, 50_000L);
        addDiscordServerWhitelist(serverId, name, true);
        return serverId;
    }

    @Transactional
    public DiscordServerWhitelistEntity updateDiscordServerWhitelist(String serverId, boolean allowed) {
        var entity = discordServerWhitelistRepository.findByDiscordServerId(serverId);
        entity.setAllowed(allowed);
        entity.setLastUpdate(now());
        return discordServerWhitelistRepository.save(entity);
    }

    @Transactional
    public DiscordServerEntity updateDiscordServerLimits(String discordServerId, long monthlyMaxTotalCharacters, long monthlyMaxTotalCharactersPerUser) {
        var entity = discordServerRepository.findById(discordServerId).get();
        entity.setMonthlyMaxTotalCharacters(monthlyMaxTotalCharacters);
        entity.setMonthlyMaxTotalCharactersPerUser(monthlyMaxTotalCharactersPerUser);
        entity.setLastUpdate(now());
        return discordServerRepository.save(entity);
    }

    public String getInfo() {
        var sb = new StringBuilder();
        var servers = discordServerRepository.findAll();
        servers.sort(Comparator.comparing(DiscordServerEntity::getName));
        var today = LocalDate.now();
        var from = LocalDate.of(today.getYear(), today.getMonth(), 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
        for (var server : servers) {
            var totalSourceCharacters = discordServerUserMessageRepository.getTotalSourceLength(from, server.getDiscordServerId());
            var totalMessagesCount = discordServerUserMessageRepository.getTotalMessagesCount(from, server.getDiscordServerId());
            sb.append("\n- ").append(server.getDiscordServerId()).append(" | ").append(server.getName()) //
                    .append("\n  max chars per month: ").append(server.getMonthlyMaxTotalCharacters()) //
                    .append("\n  max chars per month, per user: ").append(server.getMonthlyMaxTotalCharactersPerUser()) //
                    .append("\n  used quota: ").append(totalSourceCharacters) //
                    .append("\n  total translated messages: ").append(totalMessagesCount) //
            ;
        }
        return sb.toString();
    }

    protected DiscordServerEntity addDiscordServer(String serverId, String name, long monthlyMaxTotalCharacters, long monthlyMaxTotalCharactersPerUser) {
        var entity = new DiscordServerEntity();
        entity.setDiscordServerId(serverId);
        entity.setName(name);
        entity.setMonthlyMaxTotalCharacters(monthlyMaxTotalCharacters);
        entity.setMonthlyMaxTotalCharactersPerUser(monthlyMaxTotalCharactersPerUser);
        entity.setCreationDate(now());
        entity.setLastUpdate(now());
        return discordServerRepository.save(entity);
    }

    protected DiscordServerWhitelistEntity addDiscordServerWhitelist(String serverId, String description, boolean allowed) {
        var entity = new DiscordServerWhitelistEntity();
        entity.setDiscordServerId(serverId);
        entity.setDescription(description);
        entity.setAllowed(allowed);
        entity.setCreationDate(now());
        entity.setLastUpdate(now());
        return discordServerWhitelistRepository.save(entity);
    }

    private Instant now() {
        return Instant.now();
    }
}
