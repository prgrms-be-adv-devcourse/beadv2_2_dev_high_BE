package com.dev_high.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 추후에 FeignClient 으로 변경 가능
 */
public class HttpUtil {

  private HttpUtil() {
    // 인스턴스 생성 방지
  }

  /**
   * 게이트웨이 통해 호출할 때: 현재 요청 Authorization 헤더 사용
   */
  public static <T> HttpEntity<T> createGatewayEntity(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String token = getAuthorizationToken();
    if (token != null) {
      headers.setBearerAuth(token);
    }

    return new HttpEntity<>(body, headers);
  }

  /**
   * 서비스 간 직접 호출: ADMIN/System 권한  ex)유저여러명 정보 필요할때 , batch에서 수행할때(토큰 없어서 게이트웨이로 접근불가)
   *
   * @deprecated
   */
  @Deprecated
  public static <T> HttpEntity<T> createDirectEntity(T body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Role", "ADMIN");

    return new HttpEntity<>(body, headers);
  }

  /**
   * 현재 HTTP 요청에서 Authorization 헤더 가져오기
   */
  private static String getAuthorizationToken() {
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes servletAttrs) {
      HttpServletRequest request = servletAttrs.getRequest();
      return request.getHeader("Authorization");
    }
    return null; // 배치 등 요청 없으면 null 반환
  }

}