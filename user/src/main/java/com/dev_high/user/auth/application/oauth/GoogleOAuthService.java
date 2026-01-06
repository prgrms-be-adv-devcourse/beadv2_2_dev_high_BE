package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.auth.application.dto.AccessTokenResponse;
import com.dev_high.user.auth.application.dto.GoogleProfileResponse;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.user.domain.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService implements SocialOAuthService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Override
    public String getAccessToken(String code){
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenResponse> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenResponse.class);

        return response.getBody().access_token();
    }

    @Override
    public SocialProfileResponse getProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<GoogleProfileResponse> response =  restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(GoogleProfileResponse.class);
        GoogleProfileResponse googleProfile = response.getBody();
        return new SocialProfileResponse(
                getProvider(),
                googleProfile.sub(),
                googleProfile.email(),
                googleProfile.name(),
                googleProfile.name(),
                null
        );
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
    }
}
