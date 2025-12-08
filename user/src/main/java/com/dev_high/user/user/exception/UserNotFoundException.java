package com.dev_high.user.user.exception;
import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
    }

    public UserNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
