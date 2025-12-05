package it.vitalegi.translator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security.cors")
@Data
public class CorsProperties {
    List<String> allowedOrigins;
    List<String> allowedMethods;
    Boolean allowCredentials;
    List<String> allowedHeaders;
}
