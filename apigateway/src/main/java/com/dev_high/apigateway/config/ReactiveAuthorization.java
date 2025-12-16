package com.dev_high.apigateway.config;

import com.dev_high.apigateway.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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


        log.warn("[SECURITY CHECK] method={}, path={}, uri={}",
                request.getMethod(),
                request.getPath().value(),
                request.getURI());

        // Authorization 헤더 체크
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Authorization 헤더가 비어 있습니다.");
            return Mono.just(new AuthorizationDecision(false));
        }

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
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

        // Role 기반 접근 제한
        String role = claims.get("role", String.class);
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();

        if (!isAuthorized(role, path, method)) {
            log.warn("접근 거부: {} {} [{}]", method, path, role);
            return Mono.just(new AuthorizationDecision(false));
        }

        context.getExchange().getAttributes().put("User", claims);
        return Mono.just(new AuthorizationDecision(true));
    }

    private boolean isAuthorized(String role, String path, HttpMethod method) {
        return true;
    }
}
