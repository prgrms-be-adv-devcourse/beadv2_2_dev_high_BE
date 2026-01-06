package com.dev_high.user.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public record NaverProfileWrapper(
        String resultcode,
        String message,
        NaverProfile response

) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverProfile(
            String id,
            String email,
            String name,
            String nickname,
            String mobile
    ) {}
}