package com.dev_high.auction.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionRecommendationResponse(
    String productId,
    boolean available,
    String message,
    BigDecimal referencePrice,
    BigDecimal recommendedStartBid,
    BigDecimal priceRangeMin,
    BigDecimal priceRangeMax,
    AuctionAiRecommendationResult aiResult,
    OffsetDateTime recommendedStartAt,
    OffsetDateTime recommendedEndAt,
    int similarProductCount,
    int winningOrderCount,
    int auctionCount
) {
  public record AuctionAiRecommendationResult(
      BigDecimal price,
      String reason
  ) {
  }
}
