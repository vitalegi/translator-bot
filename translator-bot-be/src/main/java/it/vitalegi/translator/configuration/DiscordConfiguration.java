package it.vitalegi.translator.configuration;

import it.vitalegi.translator.discord.DiscordBot;
import it.vitalegi.translator.discord.DiscordBotImpl;
import it.vitalegi.translator.discord.OnChatInputInteraction;
import it.vitalegi.translator.discord.OnMessageCreate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DiscordConfiguration {

    @Bean
    DiscordBot discordBot(OnMessageCreate onMessageCreate, OnChatInputInteraction onChatInputInteraction, @Value("${DISCORD_TOKEN}") String token) {
        var bot = new DiscordBotImpl(onMessageCreate, onChatInputInteraction, token, "./commands");
        bot.init();
        return bot;
    }
}
