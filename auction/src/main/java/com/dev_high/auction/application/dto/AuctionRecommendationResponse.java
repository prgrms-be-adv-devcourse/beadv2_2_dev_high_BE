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
    BigDecimal winningPriceMin,
    BigDecimal winningPriceMax,
    BigDecimal winningPriceAvg,
    BigDecimal winningPriceMedian,
    BigDecimal auctionStartBidMin,
    BigDecimal auctionStartBidMax,
    BigDecimal auctionStartBidAvg,
    BigDecimal auctionStartBidMedian,
    int similarProductCount,
    int winningOrderCount,
    int auctionCount,
    int winningOrderCountPaidLike
) {
  public record AuctionAiRecommendationResult(
      BigDecimal price,
      String reason
  ) {
  }
}
