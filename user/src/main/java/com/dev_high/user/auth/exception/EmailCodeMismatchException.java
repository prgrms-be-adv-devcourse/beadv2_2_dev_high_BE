package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class EmailCodeMismatchException extends CustomException {
    public EmailCodeMismatchException() {
        super(HttpStatus.GONE, "인증번호가 올바르지 않습니다.");
    }
}