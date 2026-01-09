package com.dev_high.apigateway.repository;

import com.dev_high.apigateway.repository.dto.EndpointPolicyRow;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EndpointPolicyRepository {

    private final DatabaseClient client;

    public Flux<EndpointPolicyRow> loadAllPolicies() {
        return client.sql("""
            SELECT
                e.id              AS endpoint_id,
                e.path            AS path,
                e.method          AS method,
                e.auth_required   AS auth_required,
                COALESCE(array_agg(DISTINCT r.name) FILTER (WHERE r.name IS NOT NULL), '{}') AS roles
            FROM endpoint e
            LEFT JOIN endpoint_role er
                   ON er.endpoint_id = e.id
            LEFT JOIN "user".role r
                   ON r.id = er.role_id
            GROUP BY e.id, e.path, e.method, e.auth_required
        """)
                .map((row, meta) -> new EndpointPolicyRow(
                        row.get("endpoint_id", UUID.class),
                        row.get("path", String.class),
                        row.get("method", String.class),
                        Boolean.TRUE.equals(row.get("auth_required", Boolean.class)),
                        toRoleSet(row.get("roles"))
                ))
                .all();
    }

    private Set<String> toRoleSet(Object rolesObj) {
        if (rolesObj == null) {
            return Collections.emptySet();
        }

        if (rolesObj instanceof String[] arr) {
            return Arrays.stream(arr)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        }

        if (rolesObj instanceof Object[] arr) {
            return Arrays.stream(arr)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
        }

        String s = rolesObj.toString().trim();
        if (s.equals("{}") || s.isEmpty()) {
            return Collections.emptySet();
        }
        s = s.replace("{", "").replace("}", "");
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Mono<Long> getRuleVersion() {
        return client.sql("""
            SELECT version
            FROM public.endpoint_rule_version
            WHERE id = 1
        """)
                .map((row, meta) -> row.get("version", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }
}
