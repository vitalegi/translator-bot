package it.vitalegi.translator.auth.service;

import it.vitalegi.translator.integration.oidc.CognitoService;
import it.vitalegi.translator.integration.oidc.model.CognitoOidcResponse;
import it.vitalegi.translator.auth.model.OidcTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OidcService {

    CognitoService cognitoService;

    public OidcTokenResponse token(String code, String redirectUrl) {
        return map(cognitoService.token(code, redirectUrl));
    }

    public OidcTokenResponse refresh(String refreshToken) {
        return map(cognitoService.refresh(refreshToken));
    }

    protected OidcTokenResponse map(CognitoOidcResponse in) {
        var out = new OidcTokenResponse();
        out.setRefreshToken(in.getRefreshToken());
        out.setIdToken(in.getIdToken());
        out.setAccessToken(in.getAccessToken());
        out.setTokenType(in.getTokenType());
        out.setExpiresIn(in.getExpiresIn());
        return out;
    }
}
