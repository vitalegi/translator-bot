package it.vitalegi.translator.configuration;

import lombok.Data;

@Data
public class OidcCookieOption {
    Integer maxAge;
    Boolean secure;
    Boolean httpOnly;
    String domain;
}
