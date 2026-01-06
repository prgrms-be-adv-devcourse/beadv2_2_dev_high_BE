package com.dev_high.user.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleProfileResponse(
        String sub,
        String email,
        String name
) {
}
