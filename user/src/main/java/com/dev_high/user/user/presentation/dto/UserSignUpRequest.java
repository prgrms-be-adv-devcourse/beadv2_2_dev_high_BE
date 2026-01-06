package com.dev_high.user.user.presentation.dto;

import com.dev_high.user.user.application.dto.CreateUserCommand;

public record UserSignUpRequest(
        String email,
        String password,
        String name,
        String nickname,
        String phone_number

) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(email, password, name, nickname, phone_number);
    }
}
