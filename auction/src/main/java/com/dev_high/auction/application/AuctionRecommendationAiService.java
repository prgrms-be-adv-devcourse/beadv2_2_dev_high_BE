package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionRecommendationResponse;
import com.dev_high.auction.application.dto.AuctionRecommendationResponse.AuctionAiRecommendationResult;
import com.dev_high.auction.application.dto.ProductInfoSummary;
import com.dev_high.config.AuctionRecommendationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionRecommendationAiService {

  private final ChatClient chatClient;
  @Qualifier("auctionRecommendationTemplate")
  private final PromptTemplate auctionRecommendationTemplate;
  private final AuctionRecommendationProperties properties;
  private final ObjectMapper objectMapper;
  private final AuctionRecommendationTool auctionRecommendationTool;

  public AuctionAiRecommendationResult buildResult(
      AuctionRecommendationResponse response,
      ProductInfoSummary productInfo
  ) {
    if (!properties.isAiEnabled()) {
      return null;
    }

    try {
      Prompt prompt = auctionRecommendationTemplate.create(Map.ofEntries(
          Map.entry("productId", safe(response.productId())),
          Map.entry("productName", safe(productInfo == null ? null : productInfo.name())),
          Map.entry("productDescription", safe(productInfo == null ? null : productInfo.description())),
          Map.entry("categoryNames", safe(buildCategoryNames(productInfo))),
          Map.entry("referencePrice", safe(response.referencePrice())),
          Map.entry("priceRangeMin", safe(response.priceRangeMin())),
          Map.entry("priceRangeMax", safe(response.priceRangeMax())),
          Map.entry("winningPriceMin", safe(response.winningPriceMin())),
          Map.entry("winningPriceMax", safe(response.winningPriceMax())),
          Map.entry("winningPriceAvg", safe(response.winningPriceAvg())),
          Map.entry("winningPriceMedian", safe(response.winningPriceMedian())),
          Map.entry("auctionStartBidMin", safe(response.auctionStartBidMin())),
          Map.entry("auctionStartBidMax", safe(response.auctionStartBidMax())),
          Map.entry("auctionStartBidAvg", safe(response.auctionStartBidAvg())),
          Map.entry("auctionStartBidMedian", safe(response.auctionStartBidMedian())),
          Map.entry("similarCount", response.similarProductCount()),
          Map.entry("winningCount", response.winningOrderCount()),
          Map.entry("auctionCount", response.auctionCount()),
          Map.entry("dataNotes", buildDataNotes(response))
      ));

      ChatResponse chatResponse = chatClient.prompt(prompt)
          .tools(auctionRecommendationTool)
          .call()
          .chatResponse();
      Generation generation = chatResponse.getResult();
      String content = generation.getOutput().getText();
      if (content == null || content.isBlank()) {
        return null;
      }
      String json = extractJson(content);
      if (json == null || json.isBlank()) {
        return null;
      }
      AuctionAiRecommendationResult result = objectMapper.readValue(
          json, AuctionAiRecommendationResult.class);
      if (result == null || result.reason() == null || result.reason().isBlank()) {
        return null;
      }
      return result;
    } catch (Exception e) {
      log.warn("ai recommendation failed: {}", e.getMessage());
      return null;
    }
  }

  private String buildDataNotes(AuctionRecommendationResponse response) {
    StringBuilder sb = new StringBuilder();
    if (response.similarProductCount() <= 0) {
      sb.append("유사 상품 데이터가 부족합니다. ");
    }
    if (response.winningOrderCount() <= 0) {
      sb.append("낙찰 데이터가 부족합니다. ");
    }
    if (response.auctionCount() <= 0) {
      sb.append("등록된 경매 데이터가 부족합니다. ");
    }
    return sb.toString().trim();
  }

  private String buildCategoryIds(ProductInfoSummary productInfo) {
    if (productInfo == null || productInfo.categories() == null) {
      return "";
    }
    return productInfo.categories().stream()
        .map(ProductInfoSummary.CategoryInfo::id)
        .filter(value -> value != null && !value.isBlank())
        .distinct()
        .reduce((a, b) -> a + "," + b)
        .orElse("");
  }

  private String buildCategoryNames(ProductInfoSummary productInfo) {
    if (productInfo == null || productInfo.categories() == null) {
      return "";
    }
    return productInfo.categories().stream()
        .map(ProductInfoSummary.CategoryInfo::name)
        .filter(value -> value != null && !value.isBlank())
        .distinct()
        .reduce((a, b) -> a + "," + b)
        .orElse("");
  }

  private String safe(Object value) {
    return value == null ? "" : value.toString();
  }

  private String extractJson(String content) {
    String trimmed = content.trim();
    if (trimmed.startsWith("```")) {
      int start = trimmed.indexOf('{');
      int end = trimmed.lastIndexOf('}');
      if (start >= 0 && end > start) {
        return trimmed.substring(start, end + 1);
      }
    }
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
      return trimmed;
    }
    int start = trimmed.indexOf('{');
    int end = trimmed.lastIndexOf('}');
    if (start >= 0 && end > start) {
      return trimmed.substring(start, end + 1);
    }
    return null;
  }
}
