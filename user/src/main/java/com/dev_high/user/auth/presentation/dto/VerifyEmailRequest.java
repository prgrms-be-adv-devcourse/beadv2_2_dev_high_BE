package com.dev_high.user.auth.presentation.dto;

import com.dev_high.user.auth.application.dto.VerifyEmailCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,
        String code
) {
    public VerifyEmailCommand toCommand() {
        return new VerifyEmailCommand(email, code);
    }
}
