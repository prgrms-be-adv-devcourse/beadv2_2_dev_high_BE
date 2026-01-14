package com.dev_high.product.application.dto;

import java.util.List;

public record ProductDetailDto(
        CategoryDto category,
        String title,
        String summary,
        ConditionDto condition,
        List<String> features,
        List<String> specs,
        List<String> includedItems,
        List<String> defects,
        List<String> recommendedFor,
        List<String> searchKeywords
) {
    public record CategoryDto(
            String code,
            String name,
            Double confidence,
            List<AltDto> alternatives,
            List<String> evidence
    ) {}

    public record AltDto(String code, String name, Double confidence) {}

    public record ConditionDto(String overall, List<String> details) {}
}
