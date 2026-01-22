package com.dev_high.common.config;

import com.dev_high.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final UserContextInterceptor userContextInterceptor;

  public WebConfig(UserContextInterceptor userContextInterceptor) {
    this.userContextInterceptor = userContextInterceptor;
  }


  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 모든 경로에 적용
    registry.addInterceptor(userContextInterceptor)
            .addPathPatterns("/**");
  }
}