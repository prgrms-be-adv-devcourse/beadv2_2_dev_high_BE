package com.dev_high.search.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ProductRecommendResponse(
        String productId,
        String productName,
        List<String> categories,
        String description,
        String imageUrl,
        BigDecimal startPrice,
        BigDecimal depositAmount,
        String status,
        String sellerId,
        OffsetDateTime auctionStartAt,
        OffsetDateTime auctionEndAt,
        Double score
) {}
