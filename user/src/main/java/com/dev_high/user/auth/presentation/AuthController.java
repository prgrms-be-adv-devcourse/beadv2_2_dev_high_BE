package com.dev_high.user.auth.presentation;

import com.dev_high.user.auth.application.AuthService;
import com.dev_high.user.auth.application.dto.*;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.presentation.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send/email")
    public ApiResponseDto<Void> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        return authService.sendEmail(request.toCommand());
    }

    @PostMapping("/verify/email")
    public ApiResponseDto<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request){
        return authService.verifyEmail(request.toCommand());
    }

    @PostMapping("/login")
    public ApiResponseDto<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response){
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );
        return authService.login(command, response);
    }

    @PostMapping("/social/login")
    public ApiResponseDto<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request, HttpServletResponse response){
        return authService.socialLogin(request, response);
    }

    @PostMapping("/logout")
    public ApiResponseDto<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse response){
        return authService.logout(refreshToken, response);
    }

    @PostMapping("/refresh/token")
    public ApiResponseDto<TokenResponse> refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken){
        return authService.refreshToken(refreshToken);
    }
}
