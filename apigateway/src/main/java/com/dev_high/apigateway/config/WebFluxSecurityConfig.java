package com.dev_high.apigateway.config;

import java.util.List;
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


@Configuration
@EnableWebFluxSecurity
public class WebFluxSecurityConfig {

  private final static String[] PERMITALL_ANTPATTERNS = {
      "/", "/csrf",
      "/?*-service/swagger-ui/**",
      "/?*-service/actuator/?*", "/actuator/?*",
      "/v3/api-docs/**", "/?*-service/v3/api-docs", "/swagger*/**", "/webjars/**"
  };


  // 인증 필요없는 url
  private final static String USER_JOIN_ANTPATTERNS = "api/v1/user";
  private final static String AUTH_ANTPATTERNS = "api/v1/auth/**";
  private final static String[] AUCTION_ANTPATTERNS = {"/api/v1/auctions", "/api/v1/auctions/*",
      "/ws-auction/**"};
  private final static String[] PRODUCT_ANTPATTERNS = {"/api/v1/products", "/api/v1/products/*",
  };

  @Bean
  public SecurityWebFilterChain configure(ServerHttpSecurity http,
      ReactiveAuthorizationManager<AuthorizationContext> check) {
    http
        .httpBasic(basic -> basic.authenticationEntryPoint(
            new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))

        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .headers(headers -> headers.frameOptions(
            ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable))
        .cors(cors -> {
          cors.configurationSource(request -> {
            String path = request.getRequest().getURI().getPath();
            if (path.startsWith("/ws-auction")) {
              return null;
            }
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of("*")); // 모든 출처 허용
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setExposedHeaders(List.of("*")); // 추가
            config.setAllowCredentials(false);
            return config;
          });
        })
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers(PERMITALL_ANTPATTERNS).permitAll()
            .pathMatchers(AUTH_ANTPATTERNS).permitAll()
            .pathMatchers(HttpMethod.POST, USER_JOIN_ANTPATTERNS).permitAll()
            .pathMatchers(HttpMethod.GET, AUCTION_ANTPATTERNS).permitAll()
            .pathMatchers(HttpMethod.GET, PRODUCT_ANTPATTERNS).permitAll()
            .anyExchange().access(check)
        );

    return http.build();
  }
}