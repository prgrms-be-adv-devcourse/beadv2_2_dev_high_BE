package com.dev_high.user.auth.application.dto;

public record LoginCommand(
        String email,
        String password
) {
}
