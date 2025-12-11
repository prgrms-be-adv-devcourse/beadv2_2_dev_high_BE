package com.dev_high.user.auth.exception;

import com.dev_high.common.exception.CustomException;

public class EmailCodeMismatchException extends CustomException {
    public EmailCodeMismatchException() {
        super("잘못된 코드입니다");
    }
}