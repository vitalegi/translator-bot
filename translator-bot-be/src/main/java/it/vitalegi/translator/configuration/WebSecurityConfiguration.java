package it.vitalegi.translator.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((requests) -> requests //
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/*").permitAll() //
                        .requestMatchers("/uptime").permitAll() //
                        .requestMatchers("/oidc/token").permitAll() //
                        .requestMatchers("/oidc/refresh").permitAll() //
                        .requestMatchers("/oidc/logout").permitAll() //
                        .requestMatchers(HttpMethod.GET,"/media/cover/**").permitAll() //
                        .anyRequest().authenticated() //
                ) //
                .csrf(AbstractHttpConfigurer::disable) //
                .cors(withDefaults()) //
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults())) //
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //

                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        log.info("CORS configuration, {}", corsProperties);
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
