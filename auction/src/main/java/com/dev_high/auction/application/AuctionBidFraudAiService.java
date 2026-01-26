package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidFraudAiResult;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionBidFraudAiService {

    private final ChatClient chatClient;
    @Qualifier("auctionBidFraudTemplate")
    private final PromptTemplate auctionBidFraudTemplate;
    private final ObjectMapper objectMapper;

    public AuctionBidFraudAiResult assess(
        String auctionId,
        String userId,
        String bidPrice,
        String startBid,
        List<AuctionBidHistory> recentBids
    ) {
        try {
            Prompt prompt = auctionBidFraudTemplate.create(Map.ofEntries(
                Map.entry("startBid", safe(startBid)),
                Map.entry("recentBidsJson", toJson(recentBids))
            ));
            ChatResponse response = chatClient.prompt(prompt)
                .call()
                .chatResponse();
            Generation generation = response.getResult();
            String content = generation.getOutput().getText();
            if (content == null || content.isBlank()) {
                return null;
            }
            String json = extractJson(content);
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, AuctionBidFraudAiResult.class);
        } catch (Exception e) {
            log.warn("ai fraud check failed: {}", e.getMessage());
            return null;
        }
    }

    private String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private String toJson(List<AuctionBidHistory> recentBids) {
        if (recentBids == null || recentBids.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(recentBids.stream().map(bid -> {
                Map<String, Object> row = new HashMap<>();
                row.put("userId", bid.getUserId());
                row.put("bidPrice", bid.getBid());
                row.put("bidAt", bid.getCreatedAt().toEpochSecond());
                return row;
            }).toList());
        } catch (Exception e) {
            return "[]";
        }
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
