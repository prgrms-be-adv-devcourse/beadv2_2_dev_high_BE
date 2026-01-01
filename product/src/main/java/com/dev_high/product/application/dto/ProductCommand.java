package com.dev_high.product.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductCommand(
        String name,
        String description,
        List<String> categoryIds,
        String fileId,
        BigDecimal startBid,
        String auctionStartAt,
        String auctionEndAt
) {
}
