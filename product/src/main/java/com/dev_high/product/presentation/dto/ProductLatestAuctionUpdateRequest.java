package com.dev_high.product.presentation.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ProductLatestAuctionUpdateRequest(
    @NotBlank(message = "latestAuctionId is required")
    @JsonAlias("latest_auction_id")
    String latestAuctionId
) {
}
