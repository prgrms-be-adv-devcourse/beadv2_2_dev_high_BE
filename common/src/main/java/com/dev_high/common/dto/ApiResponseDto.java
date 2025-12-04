package com.dev_high.common.dto;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDto<T> {
  private String code;    // 비즈니스/도메인 코드
  private String message = "정상적으로 처리되었습니다.";
  private T data;

  public ApiResponseDto(T data) {
      this.data = data;
  }

  public ApiResponseDto(String message ,T data) {
    this.message = message;
    this.data = data;
  }

}

