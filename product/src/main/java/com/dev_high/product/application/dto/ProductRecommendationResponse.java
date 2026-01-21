package com.dev_high.product.application.dto;

import java.util.List;

public record ProductRecommendationResponse(
        String answer,
        List<String> productIds
) {
}
