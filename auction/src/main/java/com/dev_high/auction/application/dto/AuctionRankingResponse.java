package com.dev_high.auction.application.dto;

public record AuctionRankingResponse(
    long bidCount,
    long viewCount,
    long bidderCount,
    double score,
    AuctionResponse auction
) {
}
