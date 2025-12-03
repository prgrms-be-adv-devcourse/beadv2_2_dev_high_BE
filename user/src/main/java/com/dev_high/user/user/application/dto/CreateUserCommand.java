package com.dev_high.user.user.application.dto;

public record CreateUserCommand(
        String email,
        String password,
        String name,
        String nickname,
        String phone_number,
        String zip_code,
        String state,
        String city,
        String detail
) {
}
