package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends CustomException {
    public RefreshTokenNotFoundException() {
        super(HttpStatus.UNAUTHORIZED, "해당 Refresh Token이 존재하지 않습니다.");
    }
}
