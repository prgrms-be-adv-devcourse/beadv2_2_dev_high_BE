package com.dev_high.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

/**
 * 동일 상품에 이미 진행 중이거나 종료된 경매가 존재할 때 발생
 */
public class DuplicateAuctionException extends CustomException {

    public DuplicateAuctionException() {
        super(HttpStatus.BAD_REQUEST, "이미 진행 중이거나 종료된 경매가 존재합니다. 새 경매를 등록할 수 없습니다.");
    }

    public DuplicateAuctionException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
