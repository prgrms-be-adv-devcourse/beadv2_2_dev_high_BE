package com.dev_high.product.admin.dto;

import java.util.List;

public record AiProductGenerateRequest(
    List<CategoryCount> categories
) {
    public record CategoryCount(
        String categoryId,
        int count
    ) {}
}
