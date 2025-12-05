package com.dev_high.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

  @Bean
  @LoadBalanced  // <- 서비스 이름 기반 호출 가능
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
