package com.dev_high.product.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionCreateResponse(
        String auctionId,
        String sellerId,
        String productName,
        String status,
        BigDecimal startBid,
        BigDecimal currentBid,
        LocalDateTime auctionStartAt,
        LocalDateTime auctionEndAt
) {
}
