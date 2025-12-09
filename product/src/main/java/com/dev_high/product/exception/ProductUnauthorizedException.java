package com.dev_high.product.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ProductUnauthorizedException extends CustomException {

    public ProductUnauthorizedException() {
        super(HttpStatus.FORBIDDEN, "상품 생성자만 수정할 수 있습니다.");
    }

    public ProductUnauthorizedException(String message, String errorCode) {
        super(HttpStatus.FORBIDDEN, message, errorCode);
    }
}
