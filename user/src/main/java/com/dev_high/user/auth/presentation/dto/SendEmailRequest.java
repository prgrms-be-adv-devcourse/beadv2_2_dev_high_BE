package com.dev_high.user.auth.presentation.dto;

import com.dev_high.user.auth.application.dto.SendEmailCommand;

public record SendEmailRequest(
        String email
) {
    public SendEmailCommand toCommand() {
        return new SendEmailCommand(email);
    }
}
