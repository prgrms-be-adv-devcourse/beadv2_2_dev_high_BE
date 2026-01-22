package com.dev_high.apigateway.jwt;

import io.jsonwebtoken.Claims;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class JwtClaimUtils {

    private JwtClaimUtils() {}

    @SuppressWarnings("unchecked")
    public static Set<String> extractRoles(Claims claims) {
        if (claims == null) {
            return Set.of();
        }

        Object roles = claims.get("roles");

        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }

        if (roles instanceof String role) {
            return Set.of(role);
        }

        return Set.of();
    }
}