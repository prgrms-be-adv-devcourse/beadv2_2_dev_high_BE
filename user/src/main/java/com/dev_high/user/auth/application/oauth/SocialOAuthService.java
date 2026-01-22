package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.user.domain.OAuthProvider;

public interface SocialOAuthService {
    String getAccessToken(String code);
    SocialProfileResponse getProfile(String accessToken);
    OAuthProvider getProvider();
}
