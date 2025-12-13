package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;

public class EmailMismatchException extends CustomException {
    public EmailMismatchException() {
        super("잘못된 이메일입니다");
    }
}