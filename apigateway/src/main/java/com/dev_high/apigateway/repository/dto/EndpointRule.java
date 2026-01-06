package com.dev_high.apigateway.repository.dto;

import org.springframework.web.util.pattern.PathPattern;
import java.util.Set;

public record EndpointRule(
        PathPattern pattern,     // 컴파일된 패턴
        boolean authRequired,
        Set<String> roles
) {}
