package com.dev_high.search.application.ai;

import com.dev_high.search.application.dto.ProductRecommendResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiRecommendationSummaryGenerator {

    private static final int MAX_ITEMS = 5;

    private final ChatClient chatClient;
    private final PromptTemplate recommendationTextTemplate;

    public String summarize(List<ProductRecommendResponse> recommended, String fallbackSummary) {

        if (chatClient == null) {
            return fallbackSummary;
        }

        if (recommended == null || recommended.isEmpty()) {
            return fallbackSummary;
        }

        List<ProductRecommendResponse> safe = recommended.stream()
                .filter(Objects::nonNull)
                .limit(MAX_ITEMS)
                .toList();

        if (safe.isEmpty()) {
            return fallbackSummary;
        }

        String itemsJson = toItemsJson(safe);

        try {
            Prompt prompt = recommendationTextTemplate.create(
                    Map.of("itemsJson", itemsJson)
            );

            ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
            if (chatResponse == null) {
                return fallbackSummary;
            }

            Generation generation = chatResponse.getResult();

            String content = generation.getOutput().getText();
            if (content == null || content.isBlank()) {
                return fallbackSummary;
            }

            String oneLine = extractOneLine(content);

            if (oneLine == null || oneLine.isBlank()) {
                return fallbackSummary;
            }

            return oneLine;

        } catch (Exception e) {
            log.warn("추천 요약 생성 실패", e);
            return fallbackSummary;
        }
    }

    private static String toItemsJson(List<ProductRecommendResponse> items) {
        return items.stream()
                .map(r -> {
                    String productName = escape(r.productName());
                    String categories = toJsonArray(r.categories());
                    String description = escape(r.description());
                    double score = r.score() == null ? 0.0 : r.score();

                    return """
                        {"productName":"%s","categories":%s,"description":"%s","score":%.4f}
                        """.formatted(productName, categories, description, score).trim();
                })
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String extractOneLine(String content) {
        if (content == null) {
            return null;
        }

        String line = content.lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .findFirst()
                .orElse(null);

        if (line == null) {
            return null;
        }

        if (line.startsWith("```")) {
            line = content.lines()
                    .map(String::trim)
                    .filter(s -> !s.isBlank() && !s.startsWith("```"))
                    .findFirst()
                    .orElse(line);
        }

        if (line.length() >= 2 && line.startsWith("\"") && line.endsWith("\"")) {
            line = line.substring(1, line.length() - 1);
        }

        return line.replace("\n", " ").replace("\r", " ").trim();
    }

    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }

        return list.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> "\"" + escape(s) + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
