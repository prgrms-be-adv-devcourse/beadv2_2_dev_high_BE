package com.dev_high.user.auth.application.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String nickname,
        String role
) {
}
