package com.dev_high.product.application.dto;

public record ProductUpdateCommand(
        String name,
        String description,
        String sellerId,
        java.util.List<String> categoryIds,
        String auctionId,
        java.math.BigDecimal startBid,
        String auctionStartAt,
        String auctionEndAt
) {
}
