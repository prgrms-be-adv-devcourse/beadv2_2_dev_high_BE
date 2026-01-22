package com.dev_high.common.dto;

public record SimilarProductResponse(
    String productId,
    String auctionId,
    String imageUrl,
    double score
) {
}
