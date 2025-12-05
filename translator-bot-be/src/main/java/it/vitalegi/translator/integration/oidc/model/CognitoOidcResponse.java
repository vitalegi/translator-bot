package it.vitalegi.translator.integration.oidc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CognitoOidcResponse {
    @JsonProperty("id_token")
    String idToken;
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("refresh_token")
    String refreshToken;
    @JsonProperty("expires_in")
    int expiresIn;
    @JsonProperty("token_type")
    String tokenType;
}
