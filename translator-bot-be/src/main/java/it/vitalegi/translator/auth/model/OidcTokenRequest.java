package it.vitalegi.translator.auth.model;

import lombok.Data;

@Data
public class OidcTokenRequest {
    String code;
    String redirectUrl;
}
