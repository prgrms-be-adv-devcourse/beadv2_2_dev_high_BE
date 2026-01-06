package com.dev_high.apigateway.repository.dto;

import java.util.Set;
import java.util.UUID;

public record EndpointPolicyRow(
        UUID endpointId,
        String path,
        String method,
        boolean authRequired,
        Set<String> roles
) {}
