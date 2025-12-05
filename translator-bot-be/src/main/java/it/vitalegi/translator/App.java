package it.vitalegi.translator;

import it.vitalegi.translator.configuration.OidcProperties;
import it.vitalegi.translator.model.AppStats;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;

import java.time.Instant;

@SpringBootApplication
@EnableConfigurationProperties(OidcProperties.class)
public class App {
    public final static AppStats STATS = new AppStats();

    public static void main(String[] args) {
        STATS.setApplicationStart(Instant.now());
        SpringApplication.run(App.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        STATS.setApplicationReady(Instant.now());
    }
}
