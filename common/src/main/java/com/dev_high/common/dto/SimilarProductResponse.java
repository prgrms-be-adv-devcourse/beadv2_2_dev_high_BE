package com.dev_high.common.dto;

public record SimilarProductResponse(
    String productId,
    double score
) {
}
