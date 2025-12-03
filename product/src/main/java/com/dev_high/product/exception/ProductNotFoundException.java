package com.dev_high.product.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends CustomException {

    public ProductNotFoundException() {
        super(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
    }

    public ProductNotFoundException(String message, String errorCode) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
