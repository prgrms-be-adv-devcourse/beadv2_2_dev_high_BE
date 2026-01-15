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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiRecommendationSummaryGenerator {

    private static final int MAX_ITEMS = 10;

    private final ChatClient chatClient;

    private final  PromptTemplate recommendationTextTemplate;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public String summarize(List<ProductRecommendResponse> recommended) {
        if (chatClient == null || apiKey == null || apiKey.isBlank()) {
            return defaultSummary();
        }
        if (recommended == null || recommended.isEmpty()) {
            return defaultSummary();
        }

        List<ProductRecommendResponse> safe = recommended.stream()
                .filter(Objects::nonNull)
                .limit(MAX_ITEMS)
                .toList();

        if (safe.isEmpty()) {
            return defaultSummary();
        }

        String itemsJson = toItemsJson(safe);

        try {
            Prompt prompt = recommendationTextTemplate.create(Map.ofEntries(
                    Map.entry("itemsJson", itemsJson)
            ));

            ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
            Generation generation = chatResponse.getResult();

            String content = generation.getOutput().getText();

            log.info("원문 응답: {}", content);

            String oneLine = extractOneLine(content);

            if (oneLine == null || oneLine.isBlank()) {
                return fallbackSummary();
            }
            oneLine = normalizeLength(oneLine, 30, 60);
            if (oneLine == null) {
                return fallbackSummary();
            }

            return oneLine;

        } catch (Exception e) {
            log.warn("요약 생성 실패: {}", e.getMessage(), e);
            return fallbackSummary();
        }
    }

    private static String toItemsJson(List<ProductRecommendResponse> items) {
        return items.stream()
                .map(r -> {
                    String productName = escape(trimSafe(r.productName(), 60));
                    String categories = toJsonArray(r.categories(), 6);
                    String description = escape(trimSafe(r.description(), 160));
                    double score = (r.score() == null) ? 0.0 : r.score();

                    return """
                            {"productName":"%s","categories":%s,"description":"%s","score":%.4f}
                            """.formatted(productName, categories, description, score).trim();
                })
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String extractOneLine(String content) {
        if (content == null) return null;

        String line = content.lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .findFirst()
                .orElse(null);

        if (line == null) return null;

        if (line.startsWith("```")) {
            String trimmed = content.trim();

            line = trimmed.lines()
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

    private static String normalizeLength(String line, int min, int max) {
        if (line == null) return null;
        String s = line.trim();
        if (s.length() < min) {
            return null;
        }
        if (s.length() > max) {
            s = s.substring(0, max).trim();
            s = s.replaceAll("[\\s\\-_,]+$", "");
        }
        return s;
    }

    private static String defaultSummary() {
        return null;
    }

    private static String fallbackSummary() {
        return "찜한 상품 설명과 유사한 특징을 가진 경매 상품을 추천했어요.";
    }

    private static String trimSafe(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }

    private static String toJsonArray(List<String> list, int max) {
        if (list == null || list.isEmpty()) return "[]";

        return list.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .limit(max)
                .map(s -> "\"" + escape(s) + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
