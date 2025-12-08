package it.vitalegi.translator.util;

import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MockMvcUtil {

    public static void assert401(ResultActions resultActions) throws Exception {
        resultActions //
                .andExpect(status().is(401)) //
                .andExpect(content().string(""));
    }

    public static void assert403(ResultActions resultActions) throws Exception {
        resultActions //
                .andExpect(status().is(403)) //
                .andExpect(content().json("{'error': 'UnauthorizedAccessException'}"));
    }
}
