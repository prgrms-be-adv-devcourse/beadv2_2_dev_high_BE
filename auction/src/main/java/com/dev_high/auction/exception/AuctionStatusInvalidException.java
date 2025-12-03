package com.dev_high.auction.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AuctionStatusInvalidException extends CustomException {
    public AuctionStatusInvalidException() {
        super(HttpStatus.BAD_REQUEST, "진행된 경매는 수정할 수 없습니다. 새 경매를 생성하세요.");
    }
}
