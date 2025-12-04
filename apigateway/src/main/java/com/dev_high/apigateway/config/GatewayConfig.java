package com.dev_high.apigateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        // Auction Service
        .route("auction-service", r -> r
            .path("/api/v1/auctions/**") // 해당 경로로 요청하면 > 해당서비스로 요청전달
            .uri("lb://AUCTION-SERVICE")) // 유레카에 등록된 서비스이름

        // Notice Service
        .route("notice-service", r -> r
            .path("/api/v1/notice/**")
            .uri("lb://NOTICE-SERVICE"))

        // Deposit Service
        .route("deposit-service", r -> r
            .path("/api/v1/deposit/**")
            .uri("lb://DEPOSIT-SERVICE"))

        // Auth Service
        .route("auth-service", r -> r
            .path("/api/v1/auth/**")
            .uri("lb://AUTH"))

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
            .path("/api/v1/user/**")
            .uri("lb://USER-SERVICE"))

        .build();
  }
}