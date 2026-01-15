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
        winningPriceMin,
        winningPriceMax,
        winningPriceAvg,
        winningPriceMedian,
        auctionStartBidMin,
        auctionStartBidMax,
        auctionStartBidAvg,
        auctionStartBidMedian,
        similarProductCount,
        winningOrderCount,
        auctionCount,
        winningOrderCountPaidLike
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
      base.winningPriceMin(),
      base.winningPriceMax(),
      base.winningPriceAvg(),
      base.winningPriceMedian(),
      base.auctionStartBidMin(),
      base.auctionStartBidMax(),
      base.auctionStartBidAvg(),
      base.auctionStartBidMedian(),
      base.similarProductCount(),
      base.winningOrderCount(),
      base.auctionCount(),
      base.winningOrderCountPaidLike()
    );
  }
}
