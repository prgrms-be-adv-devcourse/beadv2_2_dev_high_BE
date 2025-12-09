package com.dev_high.user.auth.presentation.dto;

import com.dev_high.user.auth.application.dto.LoginCommand;

public record LoginRequest(
        String email,
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
