package com.dev_high.user.user.application.dto;

import com.dev_high.user.user.util.EmailMasker;

public record UserNicknameEmailResponse(
        String userId,
        String maskedEmail,
        String nickname
) {

    public static UserNicknameEmailResponse of(String userId, String email, String nickname) {
        return new UserNicknameEmailResponse(userId, EmailMasker.mask(email), nickname);
    }
}
