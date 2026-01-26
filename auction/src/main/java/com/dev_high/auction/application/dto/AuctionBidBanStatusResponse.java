package com.dev_high.auction.application.dto;

public record AuctionBidBanStatusResponse(
    boolean banned,
    String bannedUntil,
    long remainingSeconds,
    String reason
) {
    public static AuctionBidBanStatusResponse notBanned() {
        return new AuctionBidBanStatusResponse(false, null, 0L, null);
    }
}
