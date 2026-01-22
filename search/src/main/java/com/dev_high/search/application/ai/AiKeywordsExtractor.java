package com.dev_high.search.application.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiKeywordsExtractor {

    private static final int MAX_KEYWORDS_LIMIT = 20;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final PromptTemplate extractKeywordsPromptTemplate;

    public List<String> extractKeywords(String description, int maxKeywords) {
        if (chatClient == null) {
            return List.of();
        }

        String src = (description == null) ? "" : description.trim();
        if (src.isBlank()) {
            return List.of();
        }

        int limit = Math.min(maxKeywords, MAX_KEYWORDS_LIMIT);

        try {
            Prompt prompt = extractKeywordsPromptTemplate.create(
                    Map.of(
                            "limit", limit,
                            "description", src
                    )
            );

            ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
            if (chatResponse == null) {
                return fallbackKeywords(src, limit);
            }

            Generation generation = chatResponse.getResult();

            String content = generation.getOutput().getText();
            if (content == null || content.isBlank()) {
                return fallbackKeywords(src, limit);
            }

            List<String> parsed = parseKeywordsJson(content, limit);
            if (!parsed.isEmpty()) {
                return parsed;
            }

            List<String> recovered = tryRecover(content, limit);
            if (!recovered.isEmpty()) {
                return recovered;
            }

            return fallbackKeywords(src, limit);

        } catch (Exception e) {
            log.warn("AI 키워드 추출 중 오류가 발생했습니다.", e);
            return fallbackKeywords(src, limit);
        }
    }

    private List<String> parseKeywordsJson(String raw, int limit) {

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String trimmed = raw.trim()
                .replaceAll("^```(json)?", "")
                .replaceAll("```$", "")
                .trim();

        try {
            JsonNode node = objectMapper.readTree(trimmed);
            JsonNode keywordsNode = node.get("keywords");

            if (keywordsNode == null || !keywordsNode.isArray()) {
                return List.of();
            }

            Set<String> result = new LinkedHashSet<>();

            for (JsonNode k : keywordsNode) {
                if (!k.isTextual()) {
                    continue;
                }

                String keyword = normalizeKeyword(k.asText());
                if (keyword.isBlank()) {
                    continue;
                }

                if (isBanned(keyword)) {
                    continue;
                }

                result.add(keyword);

                if (result.size() >= limit) {
                    break;
                }
            }

            return new ArrayList<>(result);

        } catch (Exception e) {
            log.warn("키워드 JSON 파싱에 실패했습니다.");
            return List.of();
        }
    }

    private List<String> tryRecover(String raw, int limit) {

        if (raw == null) {
            return List.of();
        }

        String cleaned = raw.trim()
                .replaceAll("^```.*?\\n", "")
                .replaceAll("```$", "")
                .trim();

        String[] tokens = cleaned.split("[,\\n]");
        Set<String> result = new LinkedHashSet<>();

        for (String token : tokens) {
            String keyword = normalizeKeyword(token);

            if (keyword.isBlank()) {
                continue;
            }

            if (isBanned(keyword)) {
                continue;
            }

            result.add(keyword);

            if (result.size() >= limit) {
                break;
            }
        }

        return new ArrayList<>(result);
    }

    private List<String> fallbackKeywords(String src, int limit) {

        String cleaned = src
                .replaceAll("[^0-9A-Za-z가-힣\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] tokens = cleaned.split("\\s+");
        Set<String> result = new LinkedHashSet<>();

        for (String token : tokens) {
            String keyword = normalizeKeyword(token);

            if (keyword.length() < 2) {
                continue;
            }

            if (isBanned(keyword)) {
                continue;
            }

            result.add(keyword);

            if (result.size() >= limit) {
                break;
            }
        }

        return new ArrayList<>(result);
    }

    private String normalizeKeyword(String s) {

        if (s == null) {
            return "";
        }

        String t = s.trim()
                .replaceAll("^[\"'\\-•·\\s]+", "")
                .replaceAll("[\"'\\-•·\\s]+$", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);

        return t;
    }

    private boolean isBanned(String kw) {

        return kw.equals("중고")
                || kw.equals("판매")
                || kw.equals("정리")
                || kw.equals("급처")
                || kw.equals("득템")
                || kw.equals("추천")
                || kw.equals("인기")
                || kw.equals("가성비")
                || kw.equals("최상")
                || kw.equals("새상품")
                || kw.equals("미개봉")
                || kw.equals("상태좋음")
                || kw.equals("상태")
                || kw.equals("사용감")
                || kw.equals("직거래")
                || kw.equals("택배")
                || kw.equals("가능");
    }
}
