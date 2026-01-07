package com.dev_high.user.auth.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.application.dto.*;
import com.dev_high.user.auth.application.oauth.*;
import com.dev_high.user.auth.domain.*;
import com.dev_high.user.auth.exception.*;
import com.dev_high.user.auth.jwt.JwtProvider;
import com.dev_high.user.auth.presentation.dto.SocialLoginRequest;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.*;
import com.dev_high.user.user.exception.*;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService mailService;
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    private final SocialOAuthServiceFactory socialOAuthServiceFactory; // Changed from GoogleOAuthService
    private final OAuthStateContext oAuthStateContext;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public ApiResponseDto<Void> sendEmail(SendEmailCommand command) {
        String email = command.email();

        if(userRepository.existsByEmailAndDeletedYn(command.email(), "N")) {
            throw new UserAlreadyExistsException();
        }

        String authCode = this.createEmailVerificationCode();
        emailVerificationCodeRepository.save(email, authCode, 5);

        try {
            mailService.sendEmail(email, "More 이메일 인증 번호", authCode);
        } catch (MessagingException e) {
            log.error("메일 발송 실패: {}", e.getMessage(), e);
            throw new MailSendFailedException();
        }

        return ApiResponseDto.success(
                "인증 메일이 발송되었습니다. 이메일을 확인해주세요.",
                null
        );
    }

    @Transactional
    public ApiResponseDto<Void> verifyEmail(VerifyEmailCommand command) {
        Optional<String> emailVerificationCode = emailVerificationCodeRepository.findByEmail(command.email());

        if(emailVerificationCode.isEmpty()) {
            throw new EmailVerificationNotFoundException();
        }

        String savedCode = emailVerificationCode.get();
        if(!savedCode.equals(command.code())) {
            throw new EmailCodeMismatchException();
        }

        emailVerificationCodeRepository.deleteCode(command.email());

        return ApiResponseDto.success(
                "이메일 인증이 완료되었습니다. 회원가입을 계속 진행해주세요.",
                null
        );
    }

    private String createEmailVerificationCode() {
        int length = 6;
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    @Transactional
    public ApiResponseDto<LoginResponse> login(LoginCommand command, HttpServletResponse response) {
        User user = userRepository.findByEmailAndDeletedYn(command.email(), "N").orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        return generateTokensAndHandleLoginResponse(user, response);
    }
    public ApiResponseDto<TokenResponse> refreshToken(String token) {
        if(token.isEmpty()) {
            throw new RefreshTokenNotFoundException();
        }

        Claims claims = jwtProvider.parseToken(token);
        String userId = claims.getSubject();

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if(!refreshToken.getToken().equals(token)) {
            throw new RefreshTokenMismatchException();
        }

        User user = userDomainService.getUser(userId);
        Set<String> roles = userDomainService.getUserRoles(user);
        String newAccess = jwtProvider.generateAccessToken(userId, roles);
        TokenResponse tokenResponse = new TokenResponse(newAccess);

        return ApiResponseDto.success(
                "토큰이 재발급되었습니다.",
                tokenResponse
        );
    }

    public ApiResponseDto<Void> logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null) {
            try {
                Claims claims = jwtProvider.parseToken(refreshToken);
                String userId = claims.getSubject();
                refreshTokenRepository.deleteById(userId);
            } catch (Exception e) {
                log.warn("JWT 검증 실패: {}", e.getMessage(), e);
            }
        }

        deleteRefreshTokenCookie(response);

        return ApiResponseDto.success(
                "정상적으로 로그아웃되었습니다.",
                null
        );
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Transactional
    public ApiResponseDto<LoginResponse> socialLogin(SocialLoginRequest request, HttpServletResponse response) {
        OAuthProvider provider = OAuthProvider.from(request.provider());
        SocialOAuthService socialOAuthService = socialOAuthServiceFactory.getService(provider);

        if (provider == OAuthProvider.NAVER) {
            oAuthStateContext.setState(request.state());
        }

        String accessToken = socialOAuthService.getAccessToken(request.code());
        SocialProfileResponse socialProfile = socialOAuthService.getProfile(accessToken);

        Optional<User> userOptional =
                userRepository.findByProviderAndProviderUserIdAndDeletedYn(
                        socialProfile.provider(),
                        socialProfile.providerUserId(),
                        "N"
                );

        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            Optional<User> existingUser =
                    userRepository.findByEmailAndDeletedYn(socialProfile.email(), "N");
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.link(socialProfile);
            } else {
                user = userDomainService.createOAuthUser(socialProfile);
            }
        }

        return generateTokensAndHandleLoginResponse(user, response);
    }

    private ApiResponseDto<LoginResponse> generateTokensAndHandleLoginResponse(User user, HttpServletResponse response) {
        Set<String> roles = userDomainService.getUserRoles(user);
        String accessToken = jwtProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        refreshTokenRepository.save(user.getId(), refreshToken, refreshTokenExpiration);
        addRefreshTokenToCookie(response, refreshToken);

        LoginResponse loginResponse = new LoginResponse(accessToken, user.getId(), user.getNickname(), roles);

        return ApiResponseDto.success(
                "로그인에 성공했습니다.",
                loginResponse
        );
    }
}
