package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.auth.application.dto.GoogleProfileResponse;
import com.dev_high.user.auth.application.dto.OAuthTokenResponse;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.auth.exception.OAuthAccessTokenNotFoundException;
import com.dev_high.user.auth.exception.OAuthProfileInvalidException;
import com.dev_high.user.auth.exception.OAuthTokenIssueException;
import com.dev_high.user.user.domain.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
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
    public OAuthTokenResponse getTokens(String code){
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<OAuthTokenResponse> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            log.warn("구글 토큰 발급 실패. status={}", res.getStatusCode());
                            throw new ResponseStatusException(
                                    res.getStatusCode(),
                                    "구글 토큰 발급에 실패했습니다."
                            );
                        }
                )
                .toEntity(OAuthTokenResponse.class);

        OAuthTokenResponse body = response.getBody();

        if(body == null){
            log.warn("구글 토큰 응답 비어있음. status={}, headers={}", response.getStatusCode(), response.getHeaders());
            throw new OAuthTokenIssueException();
        }

        if(body.access_token() == null || body.access_token().isBlank()) {
            log.warn("구글 access_token 없음. responseBody={}", body);
            throw new OAuthAccessTokenNotFoundException();
        }

        return body;
    }

    @Override
    public SocialProfileResponse getProfile(OAuthTokenResponse tokenResponse){
        RestClient restClient = RestClient.create();
        String token = tokenResponse.access_token();
        ResponseEntity<GoogleProfileResponse> response =  restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            log.warn("구글 사용자 정보 조회 실패. status={}", res.getStatusCode());
                            throw new ResponseStatusException(
                                    res.getStatusCode(),
                                    "구글 사용자 정보를 조회하는 데 실패했습니다."
                            );
                        }
                )
                .toEntity(GoogleProfileResponse.class);

        GoogleProfileResponse googleProfile = response.getBody();

        if (googleProfile == null) {
            throw new OAuthProfileInvalidException();
        }

        return new SocialProfileResponse(
                getProvider(),
                googleProfile.sub(),
                tokenResponse.refresh_token(),
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

    @Override
    public void unlink(String refreshToken) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", refreshToken);

        restClient.post()
                .uri("https://oauth2.googleapis.com/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            log.warn("구글 계정 연동 해제 실패. status={}", res.getStatusCode());
                            throw new ResponseStatusException(
                                    res.getStatusCode(),
                                    "구글 계정 연동 해제에 실패했습니다."
                            );
                        }
                )
                .toBodilessEntity();
    }
}
