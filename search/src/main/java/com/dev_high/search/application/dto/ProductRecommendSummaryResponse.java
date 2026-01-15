package com.dev_high.search.application.dto;

import java.util.List;

public record ProductRecommendSummaryResponse(
        String summary,
        List<ProductRecommendResponse> items
) {}
