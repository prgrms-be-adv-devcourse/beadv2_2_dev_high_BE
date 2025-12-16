package com.dev_high.user.user.presentation.dto;

import com.dev_high.user.user.application.dto.UpdateUserCommand;

public record UserUpdateRequest(
        String name,
        String nickname,
        String phone_number,
        String zip_code,
        String state,
        String city,
        String detail
) {
    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(name, nickname, phone_number, zip_code, state, city, detail);
    }
}
