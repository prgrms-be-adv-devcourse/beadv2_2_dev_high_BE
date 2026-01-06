package com.dev_high.user.auth.application.dto;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String userId,
        String nickname,
        Set<String> roles
) {
}
