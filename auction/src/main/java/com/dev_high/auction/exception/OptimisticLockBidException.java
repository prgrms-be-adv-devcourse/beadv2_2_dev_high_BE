package com.dev_high.auction.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 동시 입찰로 인해 처리 실패 시 발생
 */
public class OptimisticLockBidException extends CustomException {

    public OptimisticLockBidException() {
        super(HttpStatus.CONFLICT, "동시 입찰로 인해 처리 실패, 다시 시도해주세요.");
    }

    public OptimisticLockBidException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, message, errorCode);
    }
}
