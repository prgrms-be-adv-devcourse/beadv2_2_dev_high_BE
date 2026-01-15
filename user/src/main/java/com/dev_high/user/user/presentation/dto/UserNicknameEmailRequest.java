package com.dev_high.user.user.presentation.dto;

import com.dev_high.user.user.application.dto.UserNicknameEmailCommand;
import java.util.List;

public record UserNicknameEmailRequest(
        List<String> userIds
) {
    public UserNicknameEmailCommand toCommand() {
        return new UserNicknameEmailCommand(userIds);
    }
}
