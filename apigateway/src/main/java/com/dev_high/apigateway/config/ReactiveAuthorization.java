package com.dev_high.apigateway.config;

import com.dev_high.apigateway.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveAuthorization implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final JwtProvider jwtProvider;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        ServerHttpRequest request = context.getExchange().getRequest();

        // Authorization 헤더 체크
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Authorization 헤더가 비어 있습니다.");
            return Mono.just(new AuthorizationDecision(false));
        }

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null  || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("잘못된 Authorization 헤더 형식입니다.");
            return Mono.just(new AuthorizationDecision(false));
        }

        String jwt = authorizationHeader.replace("Bearer", "").trim();
        Claims claims;
        try {
            claims = jwtProvider.parseToken(jwt);
        } catch (ExpiredJwtException e) {
            log.warn("토큰 유효기간이 만료되었습니다: {}", e.getMessage());
            return Mono.just(new AuthorizationDecision(false));
        } catch (Exception e) {
            log.warn("토큰 인증 오류: {}", e.getMessage());
            return Mono.just(new AuthorizationDecision(false));
        }

        String path = request.getPath().value();
        String role = claims.get("role", String.class);
        String method = request.getMethod().name();


        context.getExchange().getAttributes().put("User", claims);
        return Mono.just(new AuthorizationDecision(true));
    }
}