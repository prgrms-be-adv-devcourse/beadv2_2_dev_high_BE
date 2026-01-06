package com.dev_high.product.presentation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ProductLatestAuctionUpdateRequest(
    @JsonAlias("latest_auction_id")
    String latestAuctionId
) {
}
