package it.vitalegi.translator;

import it.vitalegi.translator.config.DiscordConfigurationTests;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {App.class, DiscordConfigurationTests.class})
@ActiveProfiles("test")
class AppTests {

    @Test
    void contextLoads() {
    }

}
