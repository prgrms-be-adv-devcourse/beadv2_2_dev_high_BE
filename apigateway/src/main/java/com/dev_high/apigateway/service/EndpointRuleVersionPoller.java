package com.dev_high.apigateway.service;

import com.dev_high.apigateway.repository.EndpointPolicyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointRuleVersionPoller {

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(30);
    private final EndpointPolicyRepository repository;
    private final EndpointRuleCache cache;
    private final EndpointRuleChangeListener listener;
    private Disposable worker;

    @PostConstruct
    public void start() {
        worker = Flux.interval(POLL_INTERVAL)
                .flatMap(tick -> repository.getRuleVersion()
                        .onErrorResume(e -> {
                            log.error("Rule 버전 조회 실패", e);
                            return Mono.empty();
                        }))
                .distinctUntilChanged()
                .filter(dbVer -> dbVer != cache.getLastLoadedVersion())
                .doOnNext(v -> listener.requestReload("poller"))
                .onErrorContinue((e, o) -> log.error("Rule 버전 폴링 파이프라인 오류", e))
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        if (worker != null) {
            worker.dispose();
        }
    }
}
