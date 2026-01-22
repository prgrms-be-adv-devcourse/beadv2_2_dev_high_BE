package com.dev_high.user.user.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AddressNotOwnedException extends CustomException {
    public AddressNotOwnedException() {
        super(HttpStatus.BAD_REQUEST, "해당 주소지의 소유자가 아닙니다");
    }

    public AddressNotOwnedException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
