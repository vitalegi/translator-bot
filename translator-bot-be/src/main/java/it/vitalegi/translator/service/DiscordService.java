package it.vitalegi.translator.service;

import it.vitalegi.translator.entity.DiscordServerChannelLanguageEntity;
import it.vitalegi.translator.entity.DiscordServerEntity;
import it.vitalegi.translator.entity.DiscordServerUserEntity;
import it.vitalegi.translator.entity.DiscordServerUserMessageEntity;
import it.vitalegi.translator.entity.DiscordServerWhitelistEntity;
import it.vitalegi.translator.entity.ServerChannelGroupEntity;
import it.vitalegi.translator.model.DiscordServer;
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
import java.util.List;
import java.util.Objects;
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

    public DiscordServer getDiscordServer(String serverId) {
        var entity = discordServerRepository.findById(serverId).orElseThrow(() -> new IllegalArgumentException("Server " + serverId + " not found"));
        var out = new DiscordServer();
        out.setDiscordServerId(entity.getDiscordServerId());
        out.setName(entity.getName());
        out.setMonthlyMaxTotalCharacters(entity.getMonthlyMaxTotalCharacters());
        out.setMonthlyMaxTotalCharactersPerUser(entity.getMonthlyMaxTotalCharactersPerUser());
        out.setCreationDate(entity.getCreationDate());
        out.setLastUpdate(entity.getLastUpdate());
        return out;
    }

    public String getDiscordServerChannelLanguage(String serverId, String channelName) {
        var channels = discordServerChannelLanguageRepository.findAllByServerIdAndChannelName(serverId, channelName);
        return channels.stream().filter(c -> c.getChannelName().equals(channelName)) //
                .map(DiscordServerChannelLanguageEntity::getChannelSourceLanguage) //
                .findFirst().orElse(null);
    }

    public List<DiscordServerChannelLanguageEntity> getDiscordServerChannelGroupConnectedEntries(String serverId, String channelName) {
        var channels = discordServerChannelLanguageRepository.findAllByServerId(serverId);
        var targetServerChannelGroupIds = channels.stream().filter(c -> c.getChannelName().equals(channelName)) //
                .map(DiscordServerChannelLanguageEntity::getServerChannelGroup) //
                .map(ServerChannelGroupEntity::getServerChannelGroupId) //
                .toList();
        log.info("Target groups: {}", targetServerChannelGroupIds);
        return channels.stream() //
                .filter(c -> targetServerChannelGroupIds.contains(c.getServerChannelGroup().getServerChannelGroupId())) //
                .peek(c -> log.info("connected channel: {}", c)).toList();
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
        var entries = discordServerChannelLanguageRepository.findAllByServerIdAndChannelGroupId(serverId, channelGroupId);
        var entity = entries.stream().filter(e -> e.getChannelName().equals(channel)).findFirst().orElse(null);
        if (entity != null) {
            discordServerChannelLanguageRepository.delete(entity);
        }
        return entity;
    }

    public boolean isServerAllowed(String serverId) {
        var entity = discordServerWhitelistRepository.findByDiscordServerId(serverId);
        if (entity == null) {
            return false;
        }
        return entity.isAllowed();
    }

    public Long getServerUsedMonthlyQuota(String discordServerId) {
        var usedQuota = discordServerUserMessageRepository.getTotalSourceLength(getStartOfCurrentMonth(), discordServerId);
        return usedQuota != null ? usedQuota : 0L;
    }

    public Long getServerUsedMonthlyQuotaByUser(String discordServerId, UUID userId) {
        var usedQuota = discordServerUserMessageRepository.getTotalSourceLength(getStartOfCurrentMonth(), discordServerId, userId);
        return usedQuota != null ? usedQuota : 0L;
    }

    public String getInfo() {
        var sb = new StringBuilder();
        var servers = discordServerRepository.findAll();
        servers.sort(Comparator.comparing(DiscordServerEntity::getName));
        var from = getStartOfCurrentMonth();
        for (var server : servers) {
            var totalSourceCharacters = getServerUsedMonthlyQuota(server.getDiscordServerId());
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
                var channelLanguages = discordServerChannelLanguageRepository.findAllByServerIdAndChannelGroupId(server.getDiscordServerId(), cg.getServerChannelGroupId());
                channelLanguages.stream() //
                        //.filter(cl -> cl.getServerChannelGroup().getServerChannelGroupId().equals(cg.getServerChannelGroupId()))
                        .forEach(cl -> sb.append("\n    - ").append(cl.getChannelName()).append(": ").append(cl.getChannelSourceLanguage()));
            });
        }
        return sb.toString();
    }

    public DiscordServerUserMessageEntity addDiscordServerUserMessage(String sourceLanguage, String targetLanguage, int sourceLength, int targetLength, UUID serverChannelGroupId, String discordServerId, UUID discordUserId) {
        log.info("Store stats. from {} ({}) to {} ({}). channel={}, server={}, user={}", sourceLanguage, sourceLength, targetLanguage, targetLength, serverChannelGroupId, discordServerId, discordUserId);
        var serverChannelGroupEntity = serverChannelGroupRepository.findById(serverChannelGroupId).get();
        var userEntity = discordServerUserRepository.findById(discordUserId).get();
        var discordServerEntity = discordServerRepository.findById(discordServerId).get();
        log.info("channel: {}", serverChannelGroupEntity);
        log.info("user: {}", userEntity);
        log.info("server: {}", discordServerEntity);
        var entity = new DiscordServerUserMessageEntity();
        entity.setServerChannelGroup(serverChannelGroupEntity);
        entity.setDiscordServerUser(userEntity);
        entity.setDiscordServer(discordServerEntity);
        entity.setSourceLanguage(sourceLanguage);
        entity.setTargetLanguage(targetLanguage);
        entity.setSourceLength(sourceLength);
        entity.setTargetLength(targetLength);
        entity.setCreationDate(now());

        return discordServerUserMessageRepository.save(entity);
    }

    public DiscordServerUserEntity syncDiscordServerUser(String discordServerId, String discordUserId, String discordUsername) {
        var discordServerEntity = discordServerRepository.findById(discordServerId).get();
        var userEntity = discordServerUserRepository.findByDiscordServerIdAndUserId(discordServerEntity.getDiscordServerId(), discordUserId);
        if (userEntity != null) {
            if (!Objects.equals(userEntity.getUsername(), discordUsername)) {
                userEntity.setUsername(discordUsername);
                userEntity.setLastUpdate(now());
                return discordServerUserRepository.save(userEntity);
            }
            return userEntity;
        }
        var entity = new DiscordServerUserEntity();
        entity.setUsername(discordUsername);
        entity.setUserId(discordUserId);
        entity.setDiscordServer(discordServerEntity);
        entity.setCreationDate(now());
        entity.setLastUpdate(now());
        return discordServerUserRepository.save(entity);
    }

    public UUID findDiscordServerUserId(String discordServerId, String discordUserId) {
        var entity = discordServerUserRepository.findByDiscordServerIdAndUserId(discordServerId, discordUserId);
        if (entity == null) {
            return null;
        }
        return entity.getDiscordServerUserId();
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

    protected Instant getStartOfCurrentMonth() {
        var today = LocalDate.now();
        return LocalDate.of(today.getYear(), today.getMonth(), 1).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }
}
