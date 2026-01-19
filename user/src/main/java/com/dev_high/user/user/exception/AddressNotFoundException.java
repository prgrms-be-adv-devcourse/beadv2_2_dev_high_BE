package com.dev_high.user.user.exception;
import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AddressNotFoundException extends CustomException {
    public AddressNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 주소지입니다.");
    }

    public AddressNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
