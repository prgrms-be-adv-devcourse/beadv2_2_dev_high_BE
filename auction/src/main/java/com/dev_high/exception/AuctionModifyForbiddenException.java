package com.dev_high.exception;

import com.dev_high.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AuctionModifyForbiddenException extends CustomException {

  public AuctionModifyForbiddenException() {
    super(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
  }

  public AuctionModifyForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
