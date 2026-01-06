package com.dev_high.user.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SocialLoginRequest(
        String provider,
        String code,
        @JsonProperty(required = false)
        String state
) {}
