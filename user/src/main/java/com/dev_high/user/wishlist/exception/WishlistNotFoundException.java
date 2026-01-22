package com.dev_high.user.wishlist.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class WishlistNotFoundException extends CustomException {
    public WishlistNotFoundException() {
        super(HttpStatus.NOT_FOUND, "위시시리트로 등록되지 않은 상품입니다.");
    }

    public WishlistNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
