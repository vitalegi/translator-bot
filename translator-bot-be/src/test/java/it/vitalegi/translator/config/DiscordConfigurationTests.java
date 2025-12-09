package it.vitalegi.translator.config;

import it.vitalegi.translator.discord.DiscordBot;
import it.vitalegi.translator.discord.DiscordBotStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Slf4j
@TestConfiguration
public class DiscordConfigurationTests {
    @Primary
    @Bean
    DiscordBot discordBot() {
        return new DiscordBotStub();
    }
}
