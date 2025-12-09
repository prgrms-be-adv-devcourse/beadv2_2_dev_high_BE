package com.dev_high.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.dev_high.apigateway.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;


@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config>{

    private final JwtProvider jwtProvider;

    public static class Config {

    }

    public AuthenticationFilter(JwtProvider jwtProvider) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 제외 경로 체크
            if (isExcludedPath(request)) {
                log.info("Excluded path, skipping JWT check: {}", request.getURI());
                return chain.filter(exchange);
            }

            // Authorization 헤더 체크
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Authorization 헤더가 비어 있습니다.", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return onError(exchange, "잘못된 Authorization 헤더 형식입니다.", HttpStatus.UNAUTHORIZED);
            }

            String jwt = authorizationHeader.replace("Bearer", "").trim();

            // jwt 유효성 검증
            Claims claims;
            try {
                claims = jwtProvider.parseToken(jwt);
            } catch (JwtException | IllegalArgumentException e) {
                log.error("Invalid JWT token", e);
                return onError(exchange, "유효하지 않은 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }

            // userId를 Header로 추가
            String userId = claims.getSubject();
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            // chain에 mutate된 요청 전달
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
    }

    private boolean isExcludedPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/user") && request.getMethod() == HttpMethod.POST
                || path.startsWith("/actuator/health");
    }
}
