package com.dev_high.apigateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
public class GatewayConfig {


  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            // Auction Service
            .route("auction-service", r -> r
                    .path("/api/v1/auctions/**")
                    .uri("lb://AUCTION-SERVICE")) // 유레카에 등록된 서비스이름
            .route("auction-service-ws", r -> r
                    .path("/ws-auction/**")      // 클라이언트 WebSocket 접속 경로
                    .uri("lb://AUCTION-SERVICE")) // WebSocket 업그레이드 요청도 프록시
            .route("auction-admin-service", r -> r
                    .path("/api/v1/admin/auctions/**")
                    .uri("lb://AUCTION-SERVICE"))

            // Deposit Service
            .route("deposit-service", r -> r
                    .path("/api/v1/deposit/**")
                    .uri("lb://DEPOSIT-SERVICE"))

            // Product Service
            .route("product-service", r -> r
                    .path("/api/v1/products/**", "/api/v1/categories/**", "/api/v1/chat/**", "/api/v1/files/**")
                    .uri("lb://PRODUCT-SERVICE"))

            // Search Service
            .route("search-service", r -> r
                    .path("/api/v1/search/**")
                    .uri("lb://SEARCH-SERVICE"))

            // Settlement Service
            .route("settlement-service", r -> r
                    .path("/api/v1/settle/**","/api/v1/orders/**")
                    .uri("lb://SETTLEMENT-SERVICE"))

            // Settlement Service
            .route("settlement-admin-service", r -> r
                    .path("/api/v1/admin/settles/**","/api/v1/admin/orders/**")
                    .uri("lb://SETTLEMENT-SERVICE"))

            // User Service
            .route("user-service", r -> r
                    .path("/api/v1/users/**", "/api/v1/auth/**", "/api/v1/sellers/**", "/api/v1/notifications/**")
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
            "deposit-service", "lb://DEPOSIT-SERVICE",
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