package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class RefreshTokenMismatchException extends CustomException {
    public RefreshTokenMismatchException() {
        super(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
    }
}