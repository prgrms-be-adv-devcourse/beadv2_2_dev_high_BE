package com.dev_high.user.user.application.dto;

import com.dev_high.user.user.domain.User;

public record UserResponse(
        String email,
        String name,
        String nickname,
        String phone_number,
        String zip_code,
        String state,
        String city,
        String detail
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getZipCode(),
                user.getState(),
                user.getCity(),
                user.getDetail()
        );
    }
}
