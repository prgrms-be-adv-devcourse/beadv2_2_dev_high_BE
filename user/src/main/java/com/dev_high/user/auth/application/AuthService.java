package com.dev_high.user.auth.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.application.dto.LoginCommand;
import com.dev_high.user.auth.application.dto.LoginInfo;
import com.dev_high.user.auth.exception.IncorrectPasswordException;
import com.dev_high.user.auth.jwt.JwtProvider;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public ApiResponseDto<LoginInfo> login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email()).orElseThrow(() -> new UserNotFoundException());
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getUserRole().name());
        LoginInfo loginInfo = new LoginInfo(accessToken);
        return ApiResponseDto.success(loginInfo);
    }
}
