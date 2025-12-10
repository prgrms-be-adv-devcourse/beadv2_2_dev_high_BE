package com.dev_high.user.auth.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.application.dto.LoginCommand;
import com.dev_high.user.auth.application.dto.LoginInfo;
import com.dev_high.user.auth.application.dto.TokenCommand;
import com.dev_high.user.auth.application.dto.TokenInfo;
import com.dev_high.user.auth.exception.IncorrectPasswordException;
import com.dev_high.user.auth.jwt.JwtProvider;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.UserNotFoundException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

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
}
