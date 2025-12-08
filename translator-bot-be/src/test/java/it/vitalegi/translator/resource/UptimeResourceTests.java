package it.vitalegi.translator.resource;

import it.vitalegi.translator.App;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
public class UptimeResourceTests {
    @Autowired
    MockMvc mockMvc;

    @Test
    void when_uptime_then_validResponse() throws Exception {
        App.STATS.setApplicationStart(Instant.now());
        mockMvc.perform(get("/uptime")) //
                .andDo(print()) //
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("applicationStart").isNotEmpty()) //
                .andExpect(jsonPath("applicationReady").isNotEmpty());
    }

}
