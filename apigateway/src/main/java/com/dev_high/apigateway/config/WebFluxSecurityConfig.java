package com.dev_high.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;


@Configuration
@EnableWebFluxSecurity
public class WebFluxSecurityConfig {

    private final static String[] PERMITALL_ANTPATTERNS = {
            "/", "/csrf",
            "/?*-service/swagger-ui/**",
            "/?*-service/actuator/?*", "/actuator/?*",
            "/v3/api-docs/**", "/?*-service/v3/api-docs", "/swagger*/**", "/webjars/**"
    };

    private final static String USER_JOIN_ANTPATTERNS  = "api/v1/user";
    private final static String AUTH_ANTPATTERNS = "api/v1/auth/**";


    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http, ReactiveAuthorizationManager<AuthorizationContext> check) {
        http
                .httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .cors(configurer -> configurer.configurationSource(exchange -> {
                    CorsConfiguration corsConfigurer = new CorsConfiguration();
                    corsConfigurer.setAllowedOriginPatterns(List.of("*"));
                    corsConfigurer.setAllowedMethods(List.of("*"));
                    corsConfigurer.addAllowedHeader("*");
                    corsConfigurer.setAllowCredentials(true);
                    corsConfigurer.setMaxAge(3600L);
                    return corsConfigurer;
                }))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .headers(headers -> headers.frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PERMITALL_ANTPATTERNS).permitAll()
                        .pathMatchers(AUTH_ANTPATTERNS).permitAll()
                        .pathMatchers(HttpMethod.POST, USER_JOIN_ANTPATTERNS).permitAll()
                        .anyExchange().access(check)
                );

        return http.build();
    }
}