package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;

public record AuctionCreateRequestEvent(
    String productId,
    String productName,
    String sellerId,
    BigDecimal startBid,
    int durationHours
) {
}
