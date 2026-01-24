package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionBidSuccessEvent(
    String auctionId,
    String userId,
    BigDecimal bidPrice,
    OffsetDateTime bidAt
) {
}
