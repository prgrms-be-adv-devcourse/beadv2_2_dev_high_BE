package com.dev_high.search.application.mapper;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.dev_high.search.application.dto.ProductRecommendResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class ProductRecommendMapper {
    private ProductRecommendMapper() {}

    public static ProductRecommendResponse from(Hit<JsonNode> hit) {
        if (hit == null) {
            return null;
        }

        JsonNode src = hit.source();
        if (src == null) {
            return null;
        }

        Double score = (hit.score() == null) ? 0.0 : hit.score();

        try {
            return new ProductRecommendResponse(
                    text(src, "productId"),
                    text(src, "productName"),
                    textList(src, "categories"),
                    text(src, "description"),
                    text(src, "imageUrl"),
                    decimal(src, "startPrice"),
                    decimal(src, "depositAmount"),
                    text(src, "status"),
                    text(src, "sellerId"),
                    offsetDateTimeKst(src, "auctionStartAt"),
                    offsetDateTimeKst(src, "auctionEndAt"),
                    score
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.asText();
    }

    private static List<String> textList(JsonNode n, String field) {
        JsonNode arr = n.get(field);
        if (arr == null || !arr.isArray()) {
            return List.of();
        }

        List<String> out = new ArrayList<>();
        for (JsonNode v : arr) {
            out.add(v.asText());
        }
        return out;
    }

    private static BigDecimal decimal(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        return v.decimalValue();
    }

    private static OffsetDateTime offsetDateTimeKst(JsonNode n, String field) {
        String raw = text(n, field);
        if (raw == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}