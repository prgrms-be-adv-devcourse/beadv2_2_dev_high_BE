package com.dev_high.product.admin.dto;

import java.util.List;

public record AiProductSpec(
    Category category,
    String title,
    String summary,
    Condition condition,
    List<String> features,
    List<String> specs,
    List<String> includedItems,
    List<String> defects,
    List<String> recommendedFor,
    List<String> searchKeywords
) {
    public record Category(String code, String name) {}

    public record Condition(String overall, List<String> details) {}
}
