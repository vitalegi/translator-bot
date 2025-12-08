package it.vitalegi.translator.service;

import it.vitalegi.translator.entity.DiscordServerChannelLanguageEntity;
import it.vitalegi.translator.entity.DiscordServerEntity;
import it.vitalegi.translator.entity.DiscordServerWhitelistEntity;
import it.vitalegi.translator.entity.ServerChannelGroupEntity;
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
import java.util.UUID;

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

    public ServerChannelGroupEntity addChannelGroup(String name) {
        var entity = new ServerChannelGroupEntity();
        entity.setName(name);
        entity.setCreationDate(now());
        entity.setLastUpdate(now());
        return serverChannelGroupRepository.save(entity);
    }

    @Transactional
    public ServerChannelGroupEntity updateChannelGroup(UUID id, String name) {
        var entity = serverChannelGroupRepository.findById(id).get();
        entity.setName(name);
        entity.setLastUpdate(now());
        return serverChannelGroupRepository.save(entity);
    }

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

    @Transactional
    public DiscordServerChannelLanguageEntity addDiscordServerChannelLanguage(String channelGroupName, String serverId, String channel, String language) {
        var entity = new DiscordServerChannelLanguageEntity();
        entity.setDiscordServer(discordServerRepository.findById(serverId).get());
        entity.setServerChannelGroup(serverChannelGroupRepository.findByName(channelGroupName));
        entity.setChannelName(channel);
        entity.setChannelSourceLanguage(language);
        entity.setCreationDate(now());
        entity.setLastUpdate(now());
        return discordServerChannelLanguageRepository.save(entity);
    }

    @Transactional
    public DiscordServerChannelLanguageEntity removeDiscordServerChannelLanguage(String channelGroupName, String serverId, String channel) {
        var channelGroupId = serverChannelGroupRepository.findByName(channelGroupName).getServerChannelGroupId();
        var entries = discordServerChannelLanguageRepository.findAllByServerId(serverId, channelGroupId);
        var entity = entries.stream().filter(e -> e.getChannelName().equals(channel)).findFirst().orElse(null);
        if (entity != null) {
            discordServerChannelLanguageRepository.delete(entity);
        }
        return entity;
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
            var channelGroups = serverChannelGroupRepository.findAllByServerId(server.getDiscordServerId());

            sb.append("\n- ").append(server.getDiscordServerId()).append(" | ").append(server.getName()) //
                    .append("\n  max chars per month: ").append(server.getMonthlyMaxTotalCharacters()) //
                    .append("\n  max chars per month, per user: ").append(server.getMonthlyMaxTotalCharactersPerUser()) //
                    .append("\n  used quota: ").append(totalSourceCharacters) //
                    .append("\n  total translated messages: ").append(totalMessagesCount) //
                    .append("\n  channels:");

            channelGroups.forEach(cg -> {
                sb.append("\n  - ").append(cg.getServerChannelGroupId()).append(": ").append(cg.getName());
                sb.append("\n    entries:");
                var channelLanguages = discordServerChannelLanguageRepository.findAllByServerId(server.getDiscordServerId(), cg.getServerChannelGroupId());
                channelLanguages.stream() //
                        //.filter(cl -> cl.getServerChannelGroup().getServerChannelGroupId().equals(cg.getServerChannelGroupId()))
                        .forEach(cl -> sb.append("\n    - ").append(cl.getChannelName()).append(": ").append(cl.getChannelSourceLanguage()));
            });
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
