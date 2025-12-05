package com.dev_high.common.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponseDto<T> {
  private String code ="SUCCESS";    // 비즈니스/도메인 코드
  private String message = "정상적으로 처리되었습니다.";
  private T data;

  public static <T> ApiResponseDto<T> success(T data) {
    return new ApiResponseDto<>(data);
  }

  public static <T> ApiResponseDto<T> success(String message, T data) {
    return new ApiResponseDto<>(message, data);
  }

  // 코드, 메시지, 데이터까지 모두 커스텀
  public static <T> ApiResponseDto<T> of(String code, String message, T data) {
    return new ApiResponseDto<>(code, message, data);
  }

  public static <T> ApiResponseDto<T> fail(String message) {
    return new ApiResponseDto<>("FAIL", message, null);
  }

  public static <T> ApiResponseDto<T> fail(String message,String errorCode) {
    return new ApiResponseDto<>(errorCode, message, null);
  }


  public static <T> ApiResponseDto<T> error(String message) {
    return new ApiResponseDto<>("ERR001", message, null);
  }

  private ApiResponseDto(T data) {
      this.data = data;
  }

  private ApiResponseDto(String message ,T data) {
    this.message = message;
    this.data = data;
  }
  private ApiResponseDto(String code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

}