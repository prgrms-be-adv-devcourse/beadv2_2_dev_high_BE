package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.auth.application.dto.OAuthTokenResponse;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.user.domain.OAuthProvider;

public interface SocialOAuthService {
    OAuthTokenResponse getTokens(String code);
    SocialProfileResponse getProfile(OAuthTokenResponse tokenResponse);
    OAuthProvider getProvider();
    void unlink(String refreshToken);
}
