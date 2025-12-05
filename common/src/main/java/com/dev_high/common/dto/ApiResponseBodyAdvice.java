package com.dev_high.common.dto;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // 모든 컨트롤러 반환에 적용
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    // 이미 ApiResponseDto이면 그대로 반환
    if (body instanceof ApiResponseDto) {
      ApiResponseDto<?> dto = (ApiResponseDto<?>) body;

      // 기본 성공 코드인 경우 200
      if ("SUCCESS".equals(dto.getCode())) {
        response.setStatusCode(HttpStatus.OK);
      }

      // 다른코드 CREATED 등
      else if ("CREATED".equals(dto.getCode())) {
        response.setStatusCode(HttpStatus.CREATED);
      }


      return dto;
    }

    // ApiResponseDto가 아니면 그대로 반환
    return body;
  }
}