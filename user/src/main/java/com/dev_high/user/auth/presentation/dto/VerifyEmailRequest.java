package com.dev_high.user.auth.presentation.dto;

import com.dev_high.user.auth.application.dto.VerifyEmailCommand;

public record VerifyEmailRequest(
    String email,
    String code
) {
    public VerifyEmailCommand toCommand() {
        return new VerifyEmailCommand(email, code);
    }
}
