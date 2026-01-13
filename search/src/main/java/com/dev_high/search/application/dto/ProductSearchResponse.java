package com.dev_high.search.application.dto;

import com.dev_high.search.domain.ProductDocument;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record ProductSearchResponse(
        String productId,
        String productName,
        List<String> categories,
        String description,
        BigDecimal startPrice,
        BigDecimal depositAmount,
        String status,
        String sellerId,
        OffsetDateTime auctionStartAt,
        OffsetDateTime auctionEndAt
) {
    public static ProductSearchResponse from(ProductDocument doc) {
        return new ProductSearchResponse(
                doc.getProductId(),
                doc.getProductName(),
                doc.getCategories(),
                doc.getDescription(),
                doc.getStartPrice(),
                doc.getDepositAmount(),
                doc.getStatus(),
                doc.getSellerId(),
                doc.getAuctionStartAt() != null
                        ? doc.getAuctionStartAt().atOffset(ZoneOffset.ofHours(9))
                        : null,
                doc.getAuctionEndAt() != null
                        ? doc.getAuctionEndAt().atOffset(ZoneOffset.ofHours(9))
                        : null
        );
    }
}
