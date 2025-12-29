package com.dev_high.product.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ProductDtlNotFoundException extends CustomException {

    public ProductDtlNotFoundException() {
        super(HttpStatus.NOT_FOUND, "상품 상세 정보를 찾을 수 없습니다.");
    }

    public ProductDtlNotFoundException(String message, String errorCode) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
