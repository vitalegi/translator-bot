package it.vitalegi.translator.integration.oidc;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import it.vitalegi.translator.configuration.OidcProperties;
import it.vitalegi.translator.integration.oidc.model.CognitoOidcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CognitoService {

    WebClient webClient;
    String authorizationUrl;
    String clientId;

    public CognitoService(OidcProperties oidcProperties) {
        this.authorizationUrl = oidcProperties.getAuthorizationUrl();
        this.clientId = oidcProperties.getClientId();

        var httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) //
                .responseTimeout(Duration.ofMillis(5000)) //
                .doOnConnected(conn -> //
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS)) //
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)) //
                );

        webClient = WebClient.builder() //
                .baseUrl(authorizationUrl) //
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE) //
                .clientConnector(new ReactorClientHttpConnector(httpClient)) //
                .build();
    }

    public CognitoOidcResponse token(String code, String redirectUrl) {
        return webClient.post() //
                .uri(builder -> builder.pathSegment("oauth2", "token").build()) //
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE) //
                .body(BodyInserters //
                        .fromFormData("grant_type", "authorization_code") //
                        .with("client_id", clientId) //
                        .with("redirect_uri", redirectUrl) //
                        .with("code", code) //
                ).retrieve() //
                .bodyToMono(CognitoOidcResponse.class) //
                .block();
    }

    public CognitoOidcResponse refresh(String refreshToken) {
        return webClient.post() //
                .uri(builder -> builder.pathSegment("oauth2", "token").build()) //
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE) //
                .body(BodyInserters //
                        .fromFormData("grant_type", "refresh_token") //
                        .with("client_id", clientId) //
                        .with("refresh_token", refreshToken) //
                ).retrieve() //
                .bodyToMono(CognitoOidcResponse.class) //
                .block();
    }

}
