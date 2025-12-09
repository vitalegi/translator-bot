package it.vitalegi.translator.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GlobalCommandsRegister {

    private final RestClient restClient;

    private final String commandsDirectory;

    public GlobalCommandsRegister(RestClient restClient, String commandsDirectory) {
        this.restClient = restClient;
        this.commandsDirectory = commandsDirectory;
    }

    //Since this will only run once on startup, blocking is okay.
    protected void registerCommands() {
        //Create an ObjectMapper that supports Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below
        final ApplicationService applicationService = restClient.getApplicationService();
        final long applicationId = restClient.getApplicationId().block();

        //Get our commands json from resources as command data
        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (String json : getCommandsJson()) {
            ApplicationCommandRequest request = null;
            try {
                request = d4jMapper.getObjectMapper().readValue(json, ApplicationCommandRequest.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            commands.add(request); //Add to our array list
        }

        /* Bulk overwrite commands. This is now idempotent, so it is safe to use this even when only 1 command
        is changed/added/removed
        */
        applicationService.bulkOverwriteGlobalApplicationCommand(applicationId, commands) //
                .doOnNext(cmd -> log.info("Successfully registered Global Command {}", cmd.name())).doOnError(e -> log.error("Failed to register global commands", e)).subscribe();
    }

    private List<String> getCommandsJson() {
        var paths = getFiles();

        if (paths.isEmpty()) {
            throw new IllegalArgumentException("No command found in " + commandsDirectory);
        }
        return paths.stream().map(this::getFileContent).toList();
    }

    private List<Path> getFiles() {
        try (var s = Files.walk(Path.of(commandsDirectory))) {
            return s.filter(p -> p.getFileName().toString().endsWith(".json")) //
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileContent(Path path) {
        try {
            return String.join("\n", Files.readAllLines(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
