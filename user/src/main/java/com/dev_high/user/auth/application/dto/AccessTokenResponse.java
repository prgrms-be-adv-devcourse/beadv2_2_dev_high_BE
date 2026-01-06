package com.dev_high.user.auth.application.dto;

public record AccessTokenResponse(
        String access_token,
        String expires_in,
        String scope,
        String id_token
) {
}
