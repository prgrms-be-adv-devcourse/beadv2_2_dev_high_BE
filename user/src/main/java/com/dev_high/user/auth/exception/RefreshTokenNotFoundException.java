package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends CustomException {
    public RefreshTokenNotFoundException() {
        super(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
    }
}
