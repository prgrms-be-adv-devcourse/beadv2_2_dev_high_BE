package com.dev_high.auction.application;

final class AuctionBidBanSupport {

    private static final String BAN_KEY_PREFIX = "auction:bid:ban:";

    private AuctionBidBanSupport() {
    }

    static String banKey(String auctionId, String userId) {
        return BAN_KEY_PREFIX + auctionId + ":" + userId;
    }
}
