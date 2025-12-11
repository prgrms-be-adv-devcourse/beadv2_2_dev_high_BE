package com.dev_high.user.auth.application.dto;

public record LoginInfo(
        String accessToken,
        String refreshToken,
        String nickname,
        String role
) {
}
