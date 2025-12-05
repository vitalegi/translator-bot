package it.vitalegi.translator.configuration;

import it.vitalegi.translator.auth.model.Permission;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "security")
@Data
public class RbacProperties {
    Map<String, List<Permission>> rbac;
}
