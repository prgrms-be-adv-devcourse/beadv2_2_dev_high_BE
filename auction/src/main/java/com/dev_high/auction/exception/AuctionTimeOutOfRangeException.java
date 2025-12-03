package com.dev_high.auction.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 경매 시작 전이거나 종료 후에 입찰하려고 할 때 발생
 */
public class AuctionTimeOutOfRangeException extends CustomException {

    public AuctionTimeOutOfRangeException() {
        super(HttpStatus.BAD_REQUEST, "현재 경매 시간 범위를 벗어난 입찰입니다.");
    }

    public AuctionTimeOutOfRangeException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
