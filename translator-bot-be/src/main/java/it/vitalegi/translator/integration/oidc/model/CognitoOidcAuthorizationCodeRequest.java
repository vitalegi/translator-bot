package it.vitalegi.translator.integration.oidc.model;

import lombok.Data;

@Data
public class CognitoOidcAuthorizationCodeRequest {
    String code;
    String redirectUrl;
}
