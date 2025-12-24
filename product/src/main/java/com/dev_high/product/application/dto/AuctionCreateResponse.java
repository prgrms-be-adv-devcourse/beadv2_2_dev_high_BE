package com.dev_high.product.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionCreateResponse(
        String auctionId,
        String sellerId,
        String productName,
        String status,
        BigDecimal startBid,
        BigDecimal currentBid,
        OffsetDateTime auctionStartAt,
        OffsetDateTime auctionEndAt
) {
}
