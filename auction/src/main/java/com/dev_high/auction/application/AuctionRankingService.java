package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionRankingResponse;
import com.dev_high.auction.application.dto.AuctionResponse;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionRankingService {

  private static final String RANKING_KEY = "auction:ranking:today";
  private static final String VIEW_DEDUP_KEY_PREFIX = "auction:viewed:";
  private static final String BIDDER_SET_KEY_PREFIX = "auction:bidder:";
  private static final String FIELD_BID_COUNT = "bidCount";
  private static final String FIELD_VIEW_COUNT = "viewCount";
  private static final String FIELD_BIDDER_COUNT = "bidderCount";
  @Value("${auction.ranking.bid-weight:1.0}")
  private double bidWeight;

  @Value("${auction.ranking.view-weight:1.0}")
  private double viewWeight;

  @Value("${auction.ranking.bidder-weight:3.0}")
  private double bidderWeight;

  private final StringRedisTemplate stringRedisTemplate;
  private final AuctionRepository auctionRepository;

  @Value("${auction.ranking.view-window-minutes:5}")
  private long viewWindowMinutes;

  public void incrementBidCount(String auctionId) {
    incrementStat(auctionId, FIELD_BID_COUNT, 1L, bidWeight);
  }

  public void registerBidder(String auctionId, String userId) {
    if (auctionId == null || userId == null) {
      return;
    }

    Boolean added = stringRedisTemplate.opsForSet().add(bidderSetKey(auctionId), userId) == 1;
    if (Boolean.TRUE.equals(added)) {
      incrementStat(auctionId, FIELD_BIDDER_COUNT, 1L, bidderWeight);
    }
  }

  public void incrementViewCount(String auctionId, String dedupKey) {
    if (auctionId == null || dedupKey == null) {
      return;
    }

    String viewDedupKey = viewDedupKey(auctionId, dedupKey);
    Boolean firstView = stringRedisTemplate.opsForValue().setIfAbsent(
        viewDedupKey,
        "1",
        Duration.ofMinutes(viewWindowMinutes)
    );
    if (Boolean.TRUE.equals(firstView)) {
      incrementStat(auctionId, FIELD_VIEW_COUNT, 1L, viewWeight);
  }
  }

  public List<AuctionRankingResponse> getTodayTop(int limit) {
    if (limit <= 0) {
      return Collections.emptyList();
    }

    Set<ZSetOperations.TypedTuple<String>> topEntries =
        stringRedisTemplate.opsForZSet().reverseRangeWithScores(RANKING_KEY, 0, limit - 1);
    if (topEntries == null || topEntries.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> auctionIds = topEntries.stream()
        .map(ZSetOperations.TypedTuple::getValue)
        .filter(Objects::nonNull)
        .toList();

    Map<String, Auction> auctionMap = auctionRepository.findByIdIn(auctionIds).stream()
        .collect(Collectors.toMap(Auction::getId, Function.identity()));

    return topEntries.stream()
        .map(entry -> {
          String auctionId = entry.getValue();
          if (auctionId == null) {
            return null;
          }
          Auction auction = auctionMap.get(auctionId);
          if (auction == null) {
            return null;
          }

          String statsKey = statsKey(auctionId);
          List<Object> stats = stringRedisTemplate.opsForHash()
              .multiGet(statsKey, List.of(FIELD_BID_COUNT, FIELD_VIEW_COUNT, FIELD_BIDDER_COUNT));
          long bidCount = parseLong(stats, 0);
          long viewCount = parseLong(stats, 1);
          long bidderCount = parseLong(stats, 2);
          double score = entry.getScore() == null ? 0.0 : entry.getScore();

          return new AuctionRankingResponse(
              bidCount,
              viewCount,
              bidderCount,
              score,
              AuctionResponse.fromEntity(auction)
          );
        })
        .filter(Objects::nonNull)
        .toList();
  }

  private void incrementStat(String auctionId, String field, long delta, double weight) {
    String statsKey = statsKey(auctionId);
    stringRedisTemplate.opsForHash().increment(statsKey, field, delta);
    stringRedisTemplate.opsForZSet().incrementScore(RANKING_KEY, auctionId, delta * weight);
  }

  private String statsKey(String auctionId) {
    return auctionId;
  }

  private String viewDedupKey(String auctionId, String sessionId) {
    return VIEW_DEDUP_KEY_PREFIX + auctionId + ":" + sessionId;
  }

  private String bidderSetKey(String auctionId) {
    return BIDDER_SET_KEY_PREFIX + auctionId;
  }

  private long parseLong(List<Object> values, int index) {
    if (values == null || values.size() <= index) {
      return 0L;
    }
    Object value = values.get(index);
    if (value == null) {
      return 0L;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }
}
