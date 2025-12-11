package com.dev_high.common.util;

import org.springframework.http.HttpHeaders;

/**
 * 추후에 FeignClient 으로 변경 가능
 */
public class HttpUtil {

  private HttpUtil() {
    // 인스턴스 생성 방지
  }

  public static HttpHeaders createAdminHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Role", "ADMIN");
    headers.set("X-User-Id", "SYSTEM");

    return headers;
  }

  // gateway로 보낼때
  public static HttpHeaders createBearerHttpHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token); // Authorization: Bearer <token>
    return headers;
  }
}