package com.dev_high.apigateway.security;

import com.dev_high.apigateway.jwt.JwtClaimUtils;
import com.dev_high.apigateway.repository.dto.EndpointRule;
import com.dev_high.apigateway.service.EndpointRuleCache;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final EndpointRuleCache ruleCache;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        ServerHttpRequest request = context.getExchange().getRequest();

        String method = request.getMethod().name();
        String requestPath = request.getPath().value();

        Claims claims = context.getExchange().getAttribute("claims");
        Set<String> userRoles = JwtClaimUtils.extractRoles(claims);

        // method 기준 후보 추출 ('*' 포함)
        List<EndpointRule> candidates = ruleCache.getCandidates(method);

        // path 패턴 매칭 (가장 구체적인 룰 선택)
        EndpointRule target = selectMostSpecific(candidates, requestPath);

        // 정책 없으면 거부
        if (target == null) {
            return Mono.just(new AuthorizationDecision(false));
        }

        // 인증 불필요 endpoint
        if (!target.authRequired()) {
            return Mono.just(new AuthorizationDecision(true));
        }

        // 인증 필요하지만 claims 없음
        if (claims == null) {
            return Mono.just(new AuthorizationDecision(false));
        }

        // role 검사
        return Mono.just(decideByRoles(target.roles(), userRoles));

    }

    private AuthorizationDecision decideByRoles(Set<String> allowedRoles, Set<String> userRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            return new AuthorizationDecision(false);
        }

        // SELLER가 아닌 USER만 허용
        if (allowedRoles.size() == 1 && allowedRoles.contains("USER")) {
            return new AuthorizationDecision(userRoles.size() == 1 && userRoles.contains("USER"));
        }

        // 하나라도 포함되면 허용
        for (String role : allowedRoles) {
            if (role != null && userRoles.contains(role)) {
                return new AuthorizationDecision(true);
            }
        }

        return new AuthorizationDecision(false);
    }

    private EndpointRule selectMostSpecific(List<EndpointRule> candidates, String requestPath) {
        if (candidates == null || candidates.isEmpty()) return null;

        PathContainer pathContainer = PathContainer.parsePath(requestPath);

        return candidates.stream()
                .filter(r -> r.pattern().matches(pathContainer))
                .max((a, b) -> PathPattern.SPECIFICITY_COMPARATOR.compare(a.pattern(), b.pattern()))
                .orElse(null);
    }
}
