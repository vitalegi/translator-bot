package it.vitalegi.translator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oidc")
@Data
public class OidcProperties {
    String authorizationUrl;
    String clientId;
    OidcCookieOption cookie;
}
