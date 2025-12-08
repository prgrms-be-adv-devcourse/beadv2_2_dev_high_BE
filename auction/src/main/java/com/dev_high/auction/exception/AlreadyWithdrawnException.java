package com.dev_high.auction.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 이미 입찰을 포기한 사용자가 경매 참여를 시도할 때 발생
 */
public class AlreadyWithdrawnException extends CustomException {

    public AlreadyWithdrawnException() {
        super(HttpStatus.BAD_REQUEST, "이미 경매를 포기한 사용자입니다.");
    }

    public AlreadyWithdrawnException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
