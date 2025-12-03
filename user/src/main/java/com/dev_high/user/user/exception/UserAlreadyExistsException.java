package com.dev_high.user.user.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends CustomException {

    public UserAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다.");
    }

    public UserAlreadyExistsException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
