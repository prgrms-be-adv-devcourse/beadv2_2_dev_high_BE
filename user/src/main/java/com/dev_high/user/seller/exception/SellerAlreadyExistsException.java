package com.dev_high.user.seller.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SellerAlreadyExistsException extends CustomException {
    public SellerAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "이미 판매자로 등록된 회원입니다.");
    }

    public SellerAlreadyExistsException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
