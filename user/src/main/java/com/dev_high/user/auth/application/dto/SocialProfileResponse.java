package com.dev_high.user.auth.application.dto;

import com.dev_high.user.user.domain.OAuthProvider;

public record SocialProfileResponse(
        OAuthProvider provider,
        String providerUserId,
        String providerToken,
        String email,
        String name,
        String nickname,
        String phoneNumber
) {
}
