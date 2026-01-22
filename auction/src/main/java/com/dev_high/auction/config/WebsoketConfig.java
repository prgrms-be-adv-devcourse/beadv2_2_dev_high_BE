package com.dev_high.auction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsoketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-auction")       // 클라이언트 연결 엔드포인트
        .setAllowedOriginPatterns("*")    // CORS 허용
        .withSockJS().setSuppressCors(true);                     // SockJS fallback
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");   // 서버 → 클라이언트 전송 prefix
    registry.setApplicationDestinationPrefixes("/auctions");

  }
}
