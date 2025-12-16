package com.dev_high.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 각 서비스에 해당 CustomException을 바로 사용하거나 상속받아서 새로운 Exception 생성해서 사용
 * */
public class CustomException extends RuntimeException {
  private final HttpStatus status;
  private final String errorCode; // error code  ex: ERR001

  // 기본 상태코드 400
  public CustomException(String message) {
    super(message);
    this.status = HttpStatus.BAD_REQUEST;
    this.errorCode="null";
  }


  // 에러코드 ,메세지 변경
  public CustomException(String errorCode ,String message) {
    super(message);
    this.errorCode =errorCode;
    this.status = HttpStatus.BAD_REQUEST;
  }

  //상태코드 메세지 변경
  public CustomException( HttpStatus status,String message) {
    super(message);
    this.status = status;
    this.errorCode =null;
  }

  // 전부변경
  public CustomException(HttpStatus status,String message,String errorCode) {
    super(message);
    this.status = status;
    this.errorCode=errorCode;
  }

  public HttpStatus getStatus() {
    return status;
  }
  public String getErrorCode() {
    return errorCode;
  }
}
