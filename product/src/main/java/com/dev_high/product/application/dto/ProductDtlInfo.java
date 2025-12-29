package com.dev_high.product.application.dto;

import com.dev_high.product.domain.ProductDtl;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductDtlInfo(
        String id,
        String status,
        BigDecimal startBid,
        OffsetDateTime auctionStartAt,
        OffsetDateTime auctionEndAt,
        Long depositAmount
) {
    public static ProductDtlInfo from(ProductDtl productDtl) {
        if (productDtl == null) {
            return null;
        }

        BigDecimal startBid = productDtl.getStartBid() == null
                ? null
                : BigDecimal.valueOf(productDtl.getStartBid());

        return new ProductDtlInfo(
                productDtl.getId(),
                productDtl.getStatus(),
                startBid,
                productDtl.getAuctionStartAt(),
                productDtl.getAuctionEndAt(),
                productDtl.getDepositAmount()
        );
    }
}
