package com.dev_high.product.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ProductUpdateStatusException extends CustomException {

    public ProductUpdateStatusException() {
        super(HttpStatus.BAD_REQUEST, "READY 상태의 상품만 수정할 수 있습니다.");
    }

    public ProductUpdateStatusException(String message, String errorCode) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
