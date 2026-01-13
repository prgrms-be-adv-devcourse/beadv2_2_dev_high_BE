package com.dev_high.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 입찰가가 현재 경매가보다 낮을 때 발생
 */
public class AuctionBidTooLowException extends CustomException {

    public AuctionBidTooLowException() {
        super("입찰가가 현재 경매가보다 낮습니다.");
    }

    public AuctionBidTooLowException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
