package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class EmailVerificationNotFoundException extends CustomException {
    public EmailVerificationNotFoundException() {
        super(HttpStatus.GONE,"이메일 인증 정보가 존재하지 않거나 만료되었습니다.");
    }
}