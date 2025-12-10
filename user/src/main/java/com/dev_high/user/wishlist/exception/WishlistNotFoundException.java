package com.dev_high.user.wishlist.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class WishlistNotFoundException extends CustomException {
    public WishlistNotFoundException() {
        super(HttpStatus.BAD_REQUEST, "이미 등록된 상품입니다.");
    }

    public WishlistNotFoundException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
