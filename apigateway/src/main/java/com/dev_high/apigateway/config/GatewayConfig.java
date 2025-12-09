package com.dev_high.apigateway.config;


import com.dev_high.apigateway.filter.AuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {

  private final AuthenticationFilter authenticationFilter;

  public GatewayConfig(AuthenticationFilter authenticationFilter) {
    this.authenticationFilter = authenticationFilter;
  }

  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("*"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config); // API 경로만 적용
    source.registerCorsConfiguration("/swagger/**", config);  // Swagger

    return new CorsWebFilter(source);
  }

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        // Auction Service
        .route("auction-service", r -> r
            .path("/api/v1/auctions/**") // 해당 경로로 요청하면 > 해당서비스로 요청전달
            .uri("lb://AUCTION-SERVICE")) // 유레카에 등록된 서비스이름
        .route("auction-service-ws", r -> r
            .path("/ws-auction/**")      // 클라이언트 WebSocket 접속 경로
            .uri("lb://AUCTION-SERVICE")) // WebSocket 업그레이드 요청도 프록시

        // Notification Service
        .route("notification-service", r -> r
            .path("/api/v1/notifications/**")
            .uri("lb://NOTIFICATION-SERVICE"))

        // Deposit Service
        .route("deposit-service", r -> r
            .path("/api/v1/deposit/**")
            .uri("lb://DEPOSIT-SERVICE"))

//        // Auth Service
//        .route("auth-service", r -> r
//            .path("/api/v1/auth/**")
//            .uri("lb://AUTH"))

        // Order Service
        .route("order-service", r -> r
            .path("/api/v1/order/**")
            .uri("lb://ORDER-SERVICE"))

        // Product Service
        .route("product-service", r -> r
            .path("/api/v1/products/**")
            .uri("lb://PRODUCT-SERVICE"))

        // Search Service
        .route("search-service", r -> r
            .path("/api/v1/search/**")
            .uri("lb://SEARCH-SERVICE"))

        // Settlement Service
        .route("settlement-service", r -> r
            .path("/api/v1/settle/**")
            .uri("lb://SETTLEMENT-SERVICE"))

        // User Service
        .route("user-service", r -> r
            .path("/api/v1/user/**", "/api/v1/auth/**")
            .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
            .uri("lb://USER-SERVICE"))

        .build();
  }

  @Bean
  @Profile("!prod")
  public RouteLocator swaggerRoutes(RouteLocatorBuilder builder) {
    RouteLocatorBuilder.Builder routesBuilder = builder.routes();

    // 서비스 이름과 lb URI를 맵으로 관리
    Map<String, String> services = Map.of(
        "auction-service", "lb://AUCTION-SERVICE",
        "notice-service", "lb://NOTICE-SERVICE",
        "deposit-service", "lb://DEPOSIT-SERVICE",
        "order-service", "lb://ORDER-SERVICE",
        "product-service", "lb://PRODUCT-SERVICE",
        "search-service", "lb://SEARCH-SERVICE",
        "settlement-service", "lb://SETTLEMENT-SERVICE",
        "user-service", "lb://USER-SERVICE"
    );

    services.forEach((name, uri) -> {
      routesBuilder.route(name + "-swagger", r -> r
          .path("/swagger/" + name + "/**")
          .filters(f -> f.rewritePath("/swagger/" + name + "(/(?<segment>.*))?",
              "/v3/api-docs${segment}"))
          .uri(uri)
      );
    });

    return routesBuilder.build();
  }
}