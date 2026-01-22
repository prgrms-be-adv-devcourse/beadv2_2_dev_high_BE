package com.dev_high.user.user.application.dto;

import java.util.List;

public record UserNicknameEmailCommand(
        List<String> userIds
) {
}
