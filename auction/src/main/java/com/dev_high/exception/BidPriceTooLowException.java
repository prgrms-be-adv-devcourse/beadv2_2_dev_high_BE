package com.dev_high.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 입찰가가 현재 경매 가격보다 낮을 경우 발생
 */
public class BidPriceTooLowException extends CustomException {

    public BidPriceTooLowException() {
        super(HttpStatus.BAD_REQUEST, "현재 최고 입찰가보다 높은 금액으로 다시 입찰해 주세요.");
    }

    public BidPriceTooLowException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
