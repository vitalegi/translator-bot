package it.vitalegi.translator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "translator")
@Data
public class TranslateConfig {
    String allowedServer;
    long maxTotalCharacters;
    Map<String, String> channels;
}
