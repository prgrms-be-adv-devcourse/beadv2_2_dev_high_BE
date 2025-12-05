package com.dev_high.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 각 서비스에 해당 CustomException을 바로 사용하거나 상속받아서 새로운 Exception 생성해서 사용
 * */
public class CustomException extends RuntimeException {
  private final HttpStatus status;

  // 기본 상태코드 400
  public CustomException(String message) {
    super(message);
    this.status = HttpStatus.BAD_REQUEST;
  }

  // 상태코드 지정 가능
  public CustomException( HttpStatus status,String message) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
