package com.dev_high.user.user.presentation.dto;

import com.dev_high.user.user.application.dto.UpdateUserCommand;

public record UserUpdateRequest(
        String name,
        String nickname,
        String phone_number
) {
    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(name, nickname, phone_number);
    }
}
