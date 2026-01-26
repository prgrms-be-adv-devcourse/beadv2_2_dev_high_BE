package com.dev_high.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AuctionParticipationNotFoundException extends CustomException {
    public AuctionParticipationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "참여 기록을 찾을 수 없습니다.");
    }
}
