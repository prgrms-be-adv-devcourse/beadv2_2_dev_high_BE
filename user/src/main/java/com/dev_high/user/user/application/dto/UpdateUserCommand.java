package com.dev_high.user.user.application.dto;

public record UpdateUserCommand(
        String name,
        String nickname,
        String phone_number
) {
}
