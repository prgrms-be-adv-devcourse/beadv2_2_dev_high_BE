package com.dev_high.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class AuctionHandshakeInterceptor implements HandshakeInterceptor {

  private static final String ATTR_CLIENT_IP = "clientIp";
  private static final String ATTR_USER_AGENT = "userAgent";
  private static final List<String> IP_HEADERS = List.of("X-Forwarded-For", "X-Real-IP");

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest http = servletRequest.getServletRequest();
      String ip = extractClientIp(http);
      String userAgent = http.getHeader("User-Agent");
      if (ip != null) {
        attributes.put(ATTR_CLIENT_IP, ip);
      }
      if (userAgent != null) {
        attributes.put(ATTR_USER_AGENT, userAgent);
      }
    }
    return true;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Exception exception) {
  }

  private String extractClientIp(HttpServletRequest request) {
    for (String header : IP_HEADERS) {
      String value = request.getHeader(header);
      if (value != null && !value.isBlank()) {
        if (value.contains(",")) {
          return value.split(",")[0].trim();
        }
        return value.trim();
      }
    }
    return request.getRemoteAddr();
  }
}
