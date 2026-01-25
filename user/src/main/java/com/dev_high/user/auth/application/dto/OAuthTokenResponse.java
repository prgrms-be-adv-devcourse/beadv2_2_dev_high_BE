package com.dev_high.user.auth.application.dto;

public record OAuthTokenResponse(
        String access_token,
        String refresh_token,
        String expires_in,
        String scope,
        String id_token
) {
}
