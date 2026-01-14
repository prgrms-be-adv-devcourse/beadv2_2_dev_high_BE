package com.dev_high.common.exception;


import com.dev_high.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponseDto<Void>> handleCustomException(CustomException ex) {
    return ResponseEntity
        .status(ex.getStatus())
        .body(ApiResponseDto.fail(ex.getMessage(),ex.getErrorCode()));
  }


  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiResponseDto<Void>> handleResponseStatusException(ResponseStatusException ex) {

    return ResponseEntity
        .status(ex.getStatusCode())
        .body(ApiResponseDto.fail(ex.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponseDto<Void>> handleValidationException(
          MethodArgumentNotValidException ex) {

    String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .get(0)
            .getDefaultMessage();

    return ResponseEntity
            .status(ex.getStatusCode())
            .body(ApiResponseDto.fail(errorMessage));
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponseDto<Void>> handleOtherException(Exception ex) {

    log.error("",ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponseDto.error("서버 에러가 발생했습니다."));
  }


}
