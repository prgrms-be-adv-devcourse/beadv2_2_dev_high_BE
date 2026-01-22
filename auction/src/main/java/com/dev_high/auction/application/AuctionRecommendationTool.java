package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionRecommendationResponse.AuctionAiRecommendationResult;
import java.math.BigDecimal;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class AuctionRecommendationTool {

  @Tool(
      name = "auction_recommendation",
      description = "Return auction start bid recommendation with price and reason.",
      returnDirect = true
  )
  public AuctionAiRecommendationResult recommend(
      @ToolParam(description = "Recommended start bid price as an integer.") BigDecimal price,
      @ToolParam(description = "Reason in Korean, 2-3 sentences.") String reason
  ) {
    return new AuctionAiRecommendationResult(price, reason);
  }
}
