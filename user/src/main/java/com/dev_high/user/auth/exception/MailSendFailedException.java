package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class MailSendFailedException extends CustomException {
    public MailSendFailedException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "인증 메일 전송에 실패했습니다.");
    }
}
