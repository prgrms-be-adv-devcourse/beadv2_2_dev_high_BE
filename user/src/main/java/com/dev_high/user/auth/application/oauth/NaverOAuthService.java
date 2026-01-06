package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.auth.application.dto.AccessTokenResponse;
import com.dev_high.user.auth.application.dto.NaverProfileWrapper;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.user.domain.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class NaverOAuthService implements SocialOAuthService {

    private final OAuthStateContext oAuthStateContext;

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    @Override
    public String getAccessToken(String code){
        RestClient restClient = RestClient.create();

        String state = oAuthStateContext.getState();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("grant_type", "authorization_code");
        params.add("state", state);

        ResponseEntity<AccessTokenResponse> response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenResponse.class);

        return response.getBody().access_token();
    }

    @Override
    public SocialProfileResponse getProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<NaverProfileWrapper> response = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(NaverProfileWrapper.class);

        NaverProfileWrapper body = response.getBody();
        NaverProfileWrapper.NaverProfile naverProfile = body.response();

        return new SocialProfileResponse(
                getProvider(),
                naverProfile.id(),
                naverProfile.email(),
                naverProfile.name(),
                naverProfile.nickname(),
                naverProfile.mobile()
        );
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }
}
