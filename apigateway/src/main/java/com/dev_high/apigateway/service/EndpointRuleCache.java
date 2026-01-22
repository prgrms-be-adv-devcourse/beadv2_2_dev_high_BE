package com.dev_high.apigateway.service;

import com.dev_high.apigateway.repository.dto.EndpointRule;
import com.dev_high.apigateway.repository.EndpointPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointRuleCache {

    private final EndpointPolicyRepository repository;
    private final Map<String, List<EndpointRule>> cache = new ConcurrentHashMap<>();
    private final PathPatternParser parser = new PathPatternParser();

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        repository.loadAllPolicies()
                .map(row -> {
                    PathPattern pattern = parser.parse(row.path());
                    return new RuleWithMethod(row.method(), new EndpointRule(pattern, row.authRequired(), row.roles()));
                })
                .collectList()
                .doOnNext(list -> {
                    Map<String, List<EndpointRule>> grouped = list.stream()
                            .collect(Collectors.groupingBy(
                                    RuleWithMethod::method,
                                    Collectors.mapping(RuleWithMethod::rule, Collectors.toList())
                            ));

                    cache.clear();
                    cache.putAll(grouped);

                    log.info("룰 캐시 로딩 완료 - 규칙 수: {}, 메서드: {}", list.size(), cache.keySet());
                })
                .doOnError(e -> log.error("룰 캐시 초기 로딩 실패", e))
                .subscribe();
    }

    public List<EndpointRule> getCandidates(String method) {
        List<EndpointRule> exact = cache.getOrDefault(method, Collections.emptyList());
        List<EndpointRule> any = cache.getOrDefault("*", Collections.emptyList());

        if (exact.isEmpty()) return any;
        if (any.isEmpty()) return exact;

        return java.util.stream.Stream.concat(exact.stream(), any.stream()).toList();
    }

    private record RuleWithMethod(String method, EndpointRule rule) {}
}
