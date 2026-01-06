package com.dev_high.common.interceptor;

import com.dev_high.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {


  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) {
    // 헤더에서 사용자 정보 가져오기
    log.info("All headers: {}", Collections.list(request.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(h -> h, request::getHeader)));

    String userId = request.getHeader("X-User-Id");
    log.info("uesrId >>> {}", userId);

    String authHeader = request.getHeader("Authorization");
    String token = null;
    if (authHeader != null) {
      token = authHeader.replace("Bearer", "").trim();
    }

    UserContext.set(new UserContext.UserInfo(
            userId,
            token
    ));

    return true; // 컨트롤러로 진행
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) {
    // 요청 끝나면 ThreadLocal 해제
    UserContext.clear();
  }
}