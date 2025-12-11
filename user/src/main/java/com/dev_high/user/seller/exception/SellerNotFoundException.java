package com.dev_high.user.seller.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SellerNotFoundException extends CustomException {
    public SellerNotFoundException() {
        super(HttpStatus.BAD_REQUEST, "판매자로 등록되지 않은 회원입니다.");
    }

    public SellerNotFoundException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
