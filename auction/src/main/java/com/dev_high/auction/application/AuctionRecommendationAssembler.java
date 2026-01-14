package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionRecommendationResponse;
import com.dev_high.auction.application.dto.AuctionRecommendationResponse.AuctionAiRecommendationResult;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

@Component
public class AuctionRecommendationAssembler {

  public AuctionRecommendationResponse baseResponse(
      String productId,
      boolean available,
      String message,
      BigDecimal referencePrice,
      BigDecimal recommendedStartBid,
      BigDecimal priceRangeMin,
      BigDecimal priceRangeMax,
      OffsetDateTime recommendedStartAt,
      OffsetDateTime recommendedEndAt,
      int similarProductCount,
      int winningOrderCount,
      int auctionCount
  ) {
    return new AuctionRecommendationResponse(
        productId,
        available,
        message,
        referencePrice,
        recommendedStartBid,
        priceRangeMin,
        priceRangeMax,
        null,
        recommendedStartAt,
        recommendedEndAt,
        similarProductCount,
        winningOrderCount,
        auctionCount
    );
  }

  public AuctionRecommendationResponse withAi(
      AuctionRecommendationResponse base,
      AuctionAiRecommendationResult aiResult
  ) {
    return new AuctionRecommendationResponse(
        base.productId(),
        base.available(),
        base.message(),
        base.referencePrice(),
        base.recommendedStartBid(),
        base.priceRangeMin(),
        base.priceRangeMax(),
        aiResult,
        base.recommendedStartAt(),
        base.recommendedEndAt(),
        base.similarProductCount(),
        base.winningOrderCount(),
        base.auctionCount()
    );
  }
}
