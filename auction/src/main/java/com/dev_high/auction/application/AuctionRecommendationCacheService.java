package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionRecommendationResponse;
import com.dev_high.config.AuctionRecommendationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionRecommendationCacheService {

  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;
  private final AuctionRecommendationProperties properties;

  public AuctionRecommendationResponse get(String productId) {
    if (productId == null || productId.isBlank()) {
      return null;
    }
    String key = cacheKey(productId);
    try {
      String value = stringRedisTemplate.opsForValue().get(key);
      if (value == null || value.isBlank()) {
        return null;
      }
      return objectMapper.readValue(value, AuctionRecommendationResponse.class);
    } catch (Exception e) {
      log.warn("failed to read recommendation cache: {}", e.getMessage());
      return null;
    }
  }

  public void put(String productId, AuctionRecommendationResponse response) {
    if (productId == null || productId.isBlank() || response == null) {
      return;
    }
    String key = cacheKey(productId);
    try {
      String value = objectMapper.writeValueAsString(response);
      stringRedisTemplate.opsForValue().set(
          key,
          value,
          Duration.ofMinutes(properties.getCacheTtlMinutes())
      );
    } catch (Exception e) {
      log.warn("failed to write recommendation cache: {}", e.getMessage());
    }
  }

  private String cacheKey(String productId) {
    return "auction:recommendation:" + productId;
  }
}
