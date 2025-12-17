package it.vitalegi.translator.auth.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.vitalegi.translator.App;
import it.vitalegi.translator.config.DiscordConfigurationTests;
import it.vitalegi.translator.integration.oidc.CognitoService;
import it.vitalegi.translator.integration.oidc.model.CognitoOidcResponse;
import it.vitalegi.translator.auth.model.OidcTokenResponse;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {App.class, DiscordConfigurationTests.class})
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
public class OidcResourceTests {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CognitoService cognitoService;

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    class Token {
        @Test
        void given_validRequest_then_validResponseAndCookie() throws Exception {
            when(cognitoService.token("A", "http://localhost:8080/redirect")).thenReturn(cognitoOidcResponse());
            var result = mockMvc.perform(post("/oidc/token").contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":"A", "redirectUrl":"http://localhost:8080/redirect"}
                        """)) //
                    .andDo(print()) //
                    .andExpect(status().isOk()) //
                    .andExpect(cookie().exists("refresh_token")) //
                    .andExpect(cookie().maxAge("refresh_token", 1000)) //
                    .andExpect(cookie().path("refresh_token", "/")) //
                    .andExpect(cookie().domain("refresh_token", "google.com")) //
                    .andExpect(cookie().httpOnly("refresh_token", true)) //
                    .andExpect(cookie().sameSite("refresh_token", "None")) //
                    .andExpect(cookie().secure("refresh_token", true)) //
                    .andReturn();
            var actual = payload(result.getResponse(), OidcTokenResponse.class);
            assertEquals("it", actual.getIdToken());
            assertEquals("at", actual.getAccessToken());
            assertEquals("rt", actual.getRefreshToken());
            assertEquals("tt", actual.getTokenType());
            assertEquals(1000, actual.getExpiresIn());
        }

        @Test
        void given_invalidRequest_then_error() throws Exception {
            when(cognitoService.token("A", "http://localhost:8080/redirect")).thenThrow(new RuntimeException("Invalid params"));
            var result = mockMvc.perform(post("/oidc/token").contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":"A", "redirectUrl":"http://localhost:8080/redirect"}
                        """)) //
                    .andDo(print()) //
                    .andExpect(status().is(500)) //
                    .andExpect(cookie().doesNotExist("refresh_token")) //
                    .andReturn();
            var e = result.getResolvedException();
            assertNotNull(e);
            assertEquals(RuntimeException.class, e.getClass());
            assertEquals("Invalid params", e.getMessage());

        }
    }

    @Nested
    class Refresh {

        @Test
        void given_validRequest_then_validResponse() throws Exception {
            when(cognitoService.refresh("rt")).thenReturn(cognitoOidcResponse());
            var result = mockMvc.perform(post("/oidc/refresh").contentType(MediaType.APPLICATION_JSON).cookie(new Cookie("refresh_token", "rt"))) //
                    .andDo(print()) //
                    .andExpect(status().isOk()) //
                    .andExpect(cookie().doesNotExist("refresh_token")) //
                    .andReturn();
            var actual = payload(result.getResponse(), OidcTokenResponse.class);
            assertEquals("it", actual.getIdToken());
            assertEquals("at", actual.getAccessToken());
            assertEquals("rt", actual.getRefreshToken());
            assertEquals("tt", actual.getTokenType());
            assertEquals(1000, actual.getExpiresIn());
        }

        @Test
        void given_requestWithInvalidRefreshToken_then_error() throws Exception {
            when(cognitoService.refresh("rt")).thenThrow(new RuntimeException("Invalid token"));
            mockMvc.perform(post("/oidc/refresh").contentType(MediaType.APPLICATION_JSON).cookie(new Cookie("refresh_token", "rt"))) //
                    .andDo(print()) //
                    .andExpect(status().is(500)) //
                    .andExpect(cookie().doesNotExist("refresh_token"));
        }

        @Test
        void given_requestWithoutRefreshToken_then_error() throws Exception {
            mockMvc.perform(post("/oidc/refresh").contentType(MediaType.APPLICATION_JSON)) //
                    .andDo(print()) //
                    .andExpect(status().is(500)) //
                    .andExpect(cookie().doesNotExist("refresh_token"));
            verify(cognitoService, times(0)).refresh(any());
        }
    }

    @Nested
    class Logout {
        @Test
        void then_validResponse() throws Exception {
            mockMvc.perform(get("/oidc/logout").cookie(new Cookie("refresh_token", "rt"))) //
                    .andDo(print()) //
                    .andExpect(status().isOk()) //
                    .andExpect(cookie().exists("refresh_token")) //
                    .andExpect(cookie().maxAge("refresh_token", -1)) //
                    .andReturn();
        }
    }

    CognitoOidcResponse cognitoOidcResponse() {
        return CognitoOidcResponse.builder().accessToken("at").idToken("it").tokenType("tt").expiresIn(1000).refreshToken("rt").build();
    }

    protected <E> E payload(MockHttpServletResponse response, Class<E> clazz) {
        try {
            return objectMapper.readValue(response.getContentAsString(), clazz);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
