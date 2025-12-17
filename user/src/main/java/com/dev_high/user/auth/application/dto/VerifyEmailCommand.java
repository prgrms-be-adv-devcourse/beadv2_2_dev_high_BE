package com.dev_high.user.auth.application.dto;

public record VerifyEmailCommand(
    String email,
    String code
) {
}
