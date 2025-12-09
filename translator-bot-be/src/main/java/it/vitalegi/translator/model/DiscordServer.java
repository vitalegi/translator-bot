package it.vitalegi.translator.model;

import lombok.Data;

import java.time.Instant;

@Data
public class DiscordServer {
    private String discordServerId;
    private String name;
    private Long monthlyMaxTotalCharacters;
    private Long monthlyMaxTotalCharactersPerUser;

    private Instant creationDate;
    private Instant lastUpdate;
}
