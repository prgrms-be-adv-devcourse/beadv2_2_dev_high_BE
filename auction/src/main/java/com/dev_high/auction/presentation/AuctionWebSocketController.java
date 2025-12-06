package com.dev_high.auction.presentation;

import com.dev_high.auction.application.AuctionWebSocketService;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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

    auctionWebSocketService.joinAuction(auctionId, sessionId);
  }

  // 경매 룸 퇴장
  @MessageMapping("/leave/{auctionId}")
  public void leaveAuction(@DestinationVariable String auctionId, StompHeaderAccessor accessor) {
    String sessionId = accessor.getSessionId();

    auctionWebSocketService.leaveAuction(auctionId, sessionId);
  }

}

