package com.dev_high.auction.application;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionSummaryCacheService {

  private static final String SUMMARY_KEY_PREFIX = "auction:summary:";
  private static final String RANKING_KEY = "auction:ranking:today";

  private final StringRedisTemplate stringRedisTemplate;

  public void upsert(Auction auction, AuctionLiveState liveState) {
    if (auction == null) {
      return;
    }
    String key = summaryKey(auction.getId());
    Map<String, String> values = new HashMap<>();
    values.put("id", toString(auction.getId()));
    values.put("productId", toString(auction.getProductId()));
    values.put("status", toString(auction.getStatus()));
    values.put("startBid", toString(auction.getStartBid()));
    values.put("currentBid", toString(liveState == null ? null : liveState.getCurrentBid()));
    values.put("highestUserId", toString(liveState == null ? null : liveState.getHighestUserId()));
    values.put("auctionStartAt", toString(auction.getAuctionStartAt()));
    values.put("auctionEndAt", toString(auction.getAuctionEndAt()));
    values.put("depositAmount", toString(auction.getDepositAmount()));
    values.put("deletedYn", toString(auction.getDeletedYn()));
    stringRedisTemplate.opsForHash().putAll(key, values);
  }

  public void upsertIfRanked(Auction auction, AuctionLiveState liveState) {
    if (auction == null) {
      return;
    }
    Double score = stringRedisTemplate.opsForZSet().score(RANKING_KEY, auction.getId());
    if (score == null) {
      return;
    }
    upsert(auction, liveState);
  }

  public AuctionSummary getSummary(String auctionId) {
    if (auctionId == null || auctionId.isBlank()) {
      return null;
    }
    Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(summaryKey(auctionId));
    if (raw == null || raw.isEmpty()) {
      return null;
    }
    String id = toString(raw.get("id"));
    String productId = toString(raw.get("productId"));
    AuctionStatus status = parseStatus(raw.get("status"));
    BigDecimal startBid = parseBigDecimal(raw.get("startBid"));
    BigDecimal currentBid = parseBigDecimal(raw.get("currentBid"));
    String highestUserId = toString(raw.get("highestUserId"));
    OffsetDateTime auctionStartAt = parseOffsetDateTime(raw.get("auctionStartAt"));
    OffsetDateTime auctionEndAt = parseOffsetDateTime(raw.get("auctionEndAt"));
    BigDecimal depositAmount = parseBigDecimal(raw.get("depositAmount"));
    boolean deletedYn = "Y".equalsIgnoreCase(toString(raw.get("deletedYn")));
    return new AuctionSummary(
        id,
        productId,
        status,
        startBid,
        currentBid,
        highestUserId,
        auctionStartAt,
        auctionEndAt,
        depositAmount,
        deletedYn
    );
  }

  public void delete(String auctionId) {
    if (auctionId == null || auctionId.isBlank()) {
      return;
    }
    stringRedisTemplate.delete(summaryKey(auctionId));
  }

  private String summaryKey(String auctionId) {
    return SUMMARY_KEY_PREFIX + auctionId;
  }

  private String toString(Object value) {
    return value == null ? "" : value.toString();
  }

  private AuctionStatus parseStatus(Object value) {
    String text = toString(value);
    if (text.isBlank()) {
      return null;
    }
    try {
      return AuctionStatus.valueOf(text);
    } catch (Exception e) {
      return null;
    }
  }

  private BigDecimal parseBigDecimal(Object value) {
    String text = toString(value);
    if (text.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(text);
    } catch (Exception e) {
      return null;
    }
  }

  private OffsetDateTime parseOffsetDateTime(Object value) {
    String text = toString(value);
    if (text.isBlank()) {
      return null;
    }
    try {
      return OffsetDateTime.parse(text);
    } catch (Exception e) {
      return null;
    }
  }

  public record AuctionSummary(
      String id,
      String productId,
      AuctionStatus status,
      BigDecimal startBid,
      BigDecimal currentBid,
      String highestUserId,
      OffsetDateTime auctionStartAt,
      OffsetDateTime auctionEndAt,
      BigDecimal depositAmount,
      boolean deletedYn
  ) {
  }
}
