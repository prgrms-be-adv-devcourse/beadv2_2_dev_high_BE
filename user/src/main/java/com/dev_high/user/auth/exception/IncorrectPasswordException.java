package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends CustomException {
    public IncorrectPasswordException() {
        super(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다.");
    }
}
