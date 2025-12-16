package com.dev_high.product.application.dto;

import java.util.List;

public record ProductCreateResult(
        ProductInfo product,
        List<AuctionCreateResponse> auctions
) {
}
