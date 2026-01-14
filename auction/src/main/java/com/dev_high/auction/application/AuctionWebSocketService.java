package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidMessage;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionWebSocketService {


  private final SimpMessagingTemplate messagingTemplate;
  private final AuctionRankingService auctionRankingService;

  // auctionId → 접속자 세트
  private final Map<String, Set<String>> auctionRooms = new ConcurrentHashMap<>();

  /**
   * 입찰 성공 시 웹소켓으로 실시간 전파
   */
  public void broadcastBidSuccess(AuctionBidMessage message) {
    message.withCurrentUsers(getCurrentUserCount(message.auctionId()));

    messagingTemplate.convertAndSend("/topic/auction." + message.auctionId(), message);
  }

  /**
   * 경매 화면 입장
   */
  public void joinAuction(String auctionId, String sessionId, String viewDedupKey) {
    Set<String> users = auctionRooms.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet());

    users.add(sessionId);
    auctionRankingService.incrementViewCount(auctionId, viewDedupKey);
    if (users != null && !users.isEmpty()) {
      log.info("current user count: {}", users.size());
      Map<String, Object> payload = Map.of(
          "type", "USER_JOIN",
          "currentUsers", users.size()
      );
      messagingTemplate.convertAndSend("/topic/auction." + auctionId, payload);
    }
  }

  /**
   * 경매 화면 이탈
   */
  public void leaveAuction(String auctionId, String sessionId) {
    Set<String> users = auctionRooms.get(auctionId);
    if (users != null) {
      users.remove(sessionId);
      log.info("current users count: {}", users.size());
      if (users.isEmpty()) {
        auctionRooms.remove(auctionId);
      }
    }

    if (users != null && !users.isEmpty()) {
      Map<String, Object> payload = Map.of(
          "type", "USER_LEAVE",
          "currentUsers", users.size()
      );
      messagingTemplate.convertAndSend("/topic/auction." + auctionId, payload);
    }
  }

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    String sessionId = event.getSessionId();
    log.info("disconnect sessionId: {}", sessionId);
    auctionRooms.forEach((auctionId, users) -> {
      if (users.remove(sessionId)) {
        log.info("cur user count: {}", users.size());

        if (users.isEmpty()) {
          auctionRooms.remove(auctionId);
        }

        if (users != null && !users.isEmpty()) {
          Map<String, Object> payload = Map.of(
              "type", "USER_LEAVE",
              "currentUsers", users.size()
          );
          messagingTemplate.convertAndSend("/topic/auction." + auctionId, payload);
        }
      }
    });
  }

  /**
   * 현재 경매페이지 접속자 수 조회
   */
  public int getCurrentUserCount(String auctionId) {
    return auctionRooms.getOrDefault(auctionId, Collections.emptySet()).size();
  }
}
