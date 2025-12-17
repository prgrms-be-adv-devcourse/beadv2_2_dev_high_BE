package com.dev_high.user.auth.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.application.dto.*;
import com.dev_high.user.auth.domain.EmailVerificationCodeRepository;
import com.dev_high.user.auth.domain.RefreshToken;
import com.dev_high.user.auth.domain.RefreshTokenRepository;
import com.dev_high.user.auth.exception.*;
import com.dev_high.user.auth.jwt.JwtProvider;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.UserAlreadyExistsException;
import com.dev_high.user.user.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService mailService;
    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public ApiResponseDto<Void> sendEmail(SendEmailCommand command) {
        String email = command.email();
        if(userRepository.existsByEmail(command.email())) {
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
    public ApiResponseDto<LoginResponse> login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email()).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getUserRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getUserRole().name());

        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        refreshTokenRepository.save(user.getId(), refreshToken, refreshTokenExpiration);
        LoginResponse loginResponse = new LoginResponse(accessToken, refreshToken, user.getId(), user.getNickname(), user.getUserRole().name());

        return ApiResponseDto.success(
                "로그인에 성공했습니다.",
                loginResponse
        );
    }

    public ApiResponseDto<TokenResponse> refreshToken(TokenCommand command) {
        Claims claims = jwtProvider.parseToken(command.refreshToken());
        String userId = claims.getSubject();

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if(!refreshToken.getToken().equals(command.refreshToken())) {
            throw new RefreshTokenMismatchException();
        }

        String newAccess = jwtProvider.generateAccessToken(userId, claims.get("role", String.class));
        TokenResponse tokenResponse = new TokenResponse(newAccess);

        return ApiResponseDto.success(
                "토큰이 재발급되었습니다.",
                tokenResponse
        );
    }

    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
