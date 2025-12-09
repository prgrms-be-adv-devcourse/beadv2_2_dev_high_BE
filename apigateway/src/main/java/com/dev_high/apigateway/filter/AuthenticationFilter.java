package com.dev_high.apigateway.filter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

  @Override
  public int getOrder() {
    return -1; // 인증 이후, 라우팅 전에
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    Object attribute = exchange.getAttribute("User");

    if (attribute instanceof Claims claims) {
      String userId = claims.getSubject();

      ServerHttpRequest mutatedRequest = exchange.getRequest()
          .mutate()
          .header("X-User-Id", userId)
          .build();

      return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    return chain.filter(exchange);
  }
}