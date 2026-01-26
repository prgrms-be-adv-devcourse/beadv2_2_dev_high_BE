package com.dev_high.auction.application.dto;

public record AuctionBidFraudAiResult(
    Boolean suspected,
    String reason,
    Integer banMinutes
) {
}
