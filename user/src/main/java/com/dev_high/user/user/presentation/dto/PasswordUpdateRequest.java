package com.dev_high.user.user.presentation.dto;

import com.dev_high.user.user.application.dto.UpdatePasswordCommand;

public record PasswordUpdateRequest(
        String password
) {
    public UpdatePasswordCommand toCommand() {
        return new UpdatePasswordCommand(password);
    }
}
