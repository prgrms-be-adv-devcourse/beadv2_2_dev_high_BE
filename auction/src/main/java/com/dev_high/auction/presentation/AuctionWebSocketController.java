package com.dev_high.auction.presentation;

import com.dev_high.auction.application.AuctionWebSocketService;
import com.dev_high.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuctionWebSocketController {

  private final AuctionWebSocketService auctionWebSocketService;

  // 경매 룸 입장
  @MessageMapping("/join/{auctionId}")
  public void joinAuction(@DestinationVariable String auctionId, StompHeaderAccessor accessor) {
    String sessionId = accessor.getSessionId();

    String dedupKey = buildViewDedupKey(accessor, sessionId);
    auctionWebSocketService.joinAuction(auctionId, sessionId, dedupKey);
  }

  // 경매 룸 퇴장
  @MessageMapping("/leave/{auctionId}")
  public void leaveAuction(@DestinationVariable String auctionId, StompHeaderAccessor accessor) {
    String sessionId = accessor.getSessionId();

    auctionWebSocketService.leaveAuction(auctionId, sessionId);
  }

  private String buildViewDedupKey(StompHeaderAccessor accessor, String sessionId) {
    String userId = null;
    if (UserContext.get() != null) {
      userId = UserContext.get().userId();
    }
    if (userId != null && !userId.isBlank()) {
      return userId;
    }

    String ip = extractSessionAttribute(accessor, "clientIp");
    String userAgent = extractSessionAttribute(accessor, "userAgent");

    if (ip == null && userAgent == null) {
      return sessionId;
    }

    String normalizedIp = ip == null ? "" : ip;
    String normalizedAgent = userAgent == null ? "" : userAgent;
    return normalizedIp + "|" + normalizedAgent;
  }

  private String extractSessionAttribute(StompHeaderAccessor accessor, String name) {
    if (accessor.getSessionAttributes() == null) {
      return null;
    }
    Object value = accessor.getSessionAttributes().get(name);
    if (value == null) {
      return null;
    }
    String text = value.toString().trim();
    return text.isBlank() ? null : text;
  }

}
