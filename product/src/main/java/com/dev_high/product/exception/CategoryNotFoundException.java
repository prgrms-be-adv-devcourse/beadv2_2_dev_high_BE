package com.dev_high.product.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends CustomException {

    public CategoryNotFoundException() {
        super(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
    }

    public CategoryNotFoundException(String message, String errorCode) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
