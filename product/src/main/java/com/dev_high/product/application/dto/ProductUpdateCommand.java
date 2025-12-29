package com.dev_high.product.application.dto;

import java.util.List;
public record ProductUpdateCommand(

        String name,
        String description,
        List<String> categoryIds,
        String fileGrpId,
        java.math.BigDecimal startBid,
        String auctionStartAt,
        String auctionEndAt
) {
}
