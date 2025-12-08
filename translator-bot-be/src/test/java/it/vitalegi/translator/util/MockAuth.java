package it.vitalegi.translator.util;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockAuth {

    public static RequestPostProcessor admin() {
        return admin(UUID.randomUUID().toString());
    }

    public static RequestPostProcessor admin(String uid) {
        return user(uid, Collections.singletonList("ADMIN"));
    }

    public static RequestPostProcessor member() {
        return member(UUID.randomUUID().toString());
    }

    public static RequestPostProcessor member(String uid) {
        return user(uid, Collections.singletonList("MEMBER"));
    }

    public static RequestPostProcessor guest() {
        return guest(UUID.randomUUID().toString());
    }

    public static RequestPostProcessor guest(String uid) {
        return user(uid, Collections.emptyList());
    }

    public static RequestPostProcessor user(String uid, List<String> groups) {
        var jwt = new Jwt("token", //
                Instant.now(),//
                Instant.now().plus(10, ChronoUnit.MINUTES), //
                Map.of("kid", "123", "alg", "RS256"), Map.of("cognito:groups", groups, "sub", uid));
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt);
    }

}
