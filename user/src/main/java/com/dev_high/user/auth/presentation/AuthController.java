package com.dev_high.user.auth.presentation;


import com.dev_high.user.auth.application.AuthService;
import com.dev_high.user.auth.application.dto.LoginCommand;
import com.dev_high.user.auth.application.dto.LoginInfo;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.auth.application.dto.TokenCommand;
import com.dev_high.user.auth.application.dto.TokenInfo;
import com.dev_high.user.auth.presentation.dto.LoginRequest;
import com.dev_high.user.auth.presentation.dto.TokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponseDto<LoginInfo> login(@RequestBody LoginRequest request){
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password()
        );
        return authService.login(command);
    }

    @PostMapping("/refresh/token")
    public ApiResponseDto<TokenInfo> refreshToken(@RequestBody TokenRequest request){
        TokenCommand command = new TokenCommand(
                request.refreshToken()
        );
        return authService.refreshToken(command);
    }

}
