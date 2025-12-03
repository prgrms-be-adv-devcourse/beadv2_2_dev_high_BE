package com.dev_high.auction.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 요청한 경매가 존재하지 않을 때 발생
 */
public class AuctionNotFoundException extends CustomException {

    public AuctionNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 경매입니다.");
    }

    public AuctionNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, message, errorCode);
    }
}
