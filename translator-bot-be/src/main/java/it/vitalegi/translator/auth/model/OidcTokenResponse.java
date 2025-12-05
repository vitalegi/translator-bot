package it.vitalegi.translator.auth.model;

import lombok.Data;

@Data
public class OidcTokenResponse {
    String idToken;
    String accessToken;
    String refreshToken;
    int expiresIn;
    String tokenType;
}
