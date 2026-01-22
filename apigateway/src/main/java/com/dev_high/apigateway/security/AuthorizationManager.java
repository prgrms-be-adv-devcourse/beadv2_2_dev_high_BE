package com.dev_high.apigateway.security;

import com.dev_high.apigateway.repository.dto.EndpointRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authenticationMono, AuthorizationContext context) {

        EndpointRule target =
                (EndpointRule) context.getExchange().getAttributes().get(AuthenticationFilter.ENDPOINT_RULE);

        if (target == null) {
            return Mono.just(new AuthorizationDecision(false));
        }

        if (!target.authRequired()) {
            return Mono.just(new AuthorizationDecision(true));
        }

        return authenticationMono
                .map(auth -> {
                    if (!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                        return new AuthorizationDecision(false);
                    }

                    Set<String> userRoles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet());

                    boolean allowed = isAllowed(target.roles(), userRoles);
                    if (!allowed) {
                        log.warn("인가 실패 - 필요 역할: {}, 사용자 역할: {}", target.roles(), userRoles);
                    }

                    return new AuthorizationDecision(allowed);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));

    }

    private boolean isAllowed(Set<String> allowedRoles, Set<String> userRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty()) return false;

        for (String role : allowedRoles) {
            if (role != null && userRoles.contains(role)) {
                return true;
            }
        }

        return false;
    }
}