package com.dev_high.user.auth.application.oauth;

import com.dev_high.user.auth.application.dto.NaverProfileWrapper;
import com.dev_high.user.auth.application.dto.OAuthTokenResponse;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.auth.exception.OAuthAccessTokenNotFoundException;
import com.dev_high.user.auth.exception.OAuthProfileInvalidException;
import com.dev_high.user.auth.exception.OAuthTokenIssueException;
import com.dev_high.user.user.domain.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService implements SocialOAuthService {

    private final OAuthStateContext oAuthStateContext;

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    @Override
    public OAuthTokenResponse getTokens(String code){
        RestClient restClient = RestClient.create();

        String state = oAuthStateContext.getState();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        params.add("code", code);
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("grant_type", "authorization_code");
        params.add("state", state);

        ResponseEntity<OAuthTokenResponse> response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            log.warn("네이버 토큰 발급 실패. status={}", res.getStatusCode());
                            throw new ResponseStatusException(
                                    res.getStatusCode(),
                                    "네이버 토큰 발급에 실패했습니다."
                            );
                        }
                )
                .toEntity(OAuthTokenResponse.class);

        OAuthTokenResponse body = response.getBody();

        if(body == null){
            log.warn("네이버 토큰 응답 비어있음. status={}, headers={}", response.getStatusCode(), response.getHeaders());
            throw new OAuthTokenIssueException();
        }

        if(body.access_token() == null || body.access_token().isBlank()) {
            log.warn("네이버 access_token 없음. responseBody={}", body);
            throw new OAuthAccessTokenNotFoundException();
        }

        return body;
    }

    @Override
    public SocialProfileResponse getProfile(OAuthTokenResponse tokenResponse){
        RestClient restClient = RestClient.create();
        String token = tokenResponse.access_token();
        ResponseEntity<NaverProfileWrapper> response = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            log.warn("네이버 사용자 정보 조회 실패. status={}", res.getStatusCode());
                            throw new ResponseStatusException(
                                    res.getStatusCode(),
                                    "네이버 사용자 정보를 조회하는 데 실패했습니다."
                            );
                        }
                )
                .toEntity(NaverProfileWrapper.class);

        NaverProfileWrapper body = response.getBody();

        if (body == null || body.response() == null) {
             throw new OAuthProfileInvalidException();
        }

        NaverProfileWrapper.NaverProfile naverProfile = body.response();

        return new SocialProfileResponse(
                getProvider(),
                naverProfile.id(),
                tokenResponse.refresh_token(),
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

    public void unlink(String refreshToken) {
        Optional<String> accessToken = getAccessTokenFromRefreshToken(refreshToken);
        if (accessToken.isEmpty()) {
            throw new OAuthAccessTokenNotFoundException();
        }

        RestClient restClient = RestClient.create();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("grant_type", "delete");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("access_token", accessToken.get());
        params.add("service_provider", "NAVER");

        restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {
                            log.warn("네이버 계정 연동 해제 실패. status={}", res.getStatusCode());
                            throw new ResponseStatusException(
                                    res.getStatusCode(),
                                    "네이버 계정 연동 해제에 실패했습니다."
                            );
                        }
                )
                .toBodilessEntity();
    }

    private Optional<String> getAccessTokenFromRefreshToken(String refreshToken) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("refresh_token", refreshToken);

        return Optional.ofNullable(
                restClient.post()
                    .uri("https://nid.naver.com/oauth2.0/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (req, res) -> {
                                log.warn("네이버 토큰 갱신 실패. status={}", res.getStatusCode());
                                throw new ResponseStatusException(
                                        res.getStatusCode(),
                                        "네이버 토큰 갱신에 실패했습니다."
                                );
                            }
                    )
                    .body(OAuthTokenResponse.class)
                    ).map(OAuthTokenResponse::access_token);
    }

}
