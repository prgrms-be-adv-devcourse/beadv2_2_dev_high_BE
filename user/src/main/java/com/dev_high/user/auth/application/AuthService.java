package com.dev_high.user.auth.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.application.dto.LoginCommand;
import com.dev_high.user.auth.application.dto.LoginInfo;
import com.dev_high.user.auth.application.dto.TokenCommand;
import com.dev_high.user.auth.application.dto.TokenInfo;
import com.dev_high.user.auth.domain.EmailVerificationCodeRepository;
import com.dev_high.user.auth.exception.IncorrectPasswordException;
import com.dev_high.user.auth.exception.MailSendFailedException;
import com.dev_high.user.auth.jwt.JwtProvider;
import com.dev_high.user.user.application.UserDomainService;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.domain.UserStatus;
import com.dev_high.user.user.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService mailService;
    private final UserDomainService userDomainService;

    public ApiResponseDto<LoginInfo> login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email()).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getUserRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getUserRole().name());

        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);

        LoginInfo loginInfo = new LoginInfo(accessToken, refreshToken);
        return ApiResponseDto.success(loginInfo);
    }

    public ApiResponseDto<TokenInfo> refreshToken(TokenCommand command) {
        Claims claims = jwtProvider.parseToken(command.refreshToken());
        String userId = claims.getSubject();
        String newAccess = jwtProvider.generateAccessToken(userId, claims.get("role", String.class));
        TokenInfo tokenInfo = new TokenInfo(newAccess);
        return ApiResponseDto.success(tokenInfo);
    }

    public void sendEmail(String email) {
        String title = "More 이메일 인증 번호";
        String authCode = this.createEmailVerificationCode();
        log.info(authCode);
        emailVerificationCodeRepository.save(email, authCode, 5);
        try {
            mailService.sendEmail(email, title, authCode);
        } catch (MessagingException e) {
            throw new MailSendFailedException();
        }

    }

    @Transactional
    public ApiResponseDto<Void> verifyEmail(String email, String code) {
        Optional<String> emailVerificationCode = emailVerificationCodeRepository.findByEmail(email);
        if (emailVerificationCode.isPresent()) {
            String savedCode = emailVerificationCode.get();
            if (savedCode.equals(code)) {
                userDomainService.updateUserStatus(email, UserStatus.ACTIVE);
                emailVerificationCodeRepository.deleteCode(email);
            }
        }
        return ApiResponseDto.success(null);
    }

    private String createEmailVerificationCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }
}
