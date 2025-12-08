package it.vitalegi.translator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "discord")
@Data
public class DiscordAdminProperties {
    List<String> superAdminIds;
}
