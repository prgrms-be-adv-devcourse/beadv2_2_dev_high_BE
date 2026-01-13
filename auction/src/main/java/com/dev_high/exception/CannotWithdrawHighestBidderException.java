package com.dev_high.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 최고 입찰자가 경매를 포기하려고 할 때 발생
 */
public class CannotWithdrawHighestBidderException extends CustomException {

    public CannotWithdrawHighestBidderException() {
        super(HttpStatus.BAD_REQUEST, "최고 입찰자는 경매를 포기할 수 없습니다.");
    }

    public CannotWithdrawHighestBidderException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
