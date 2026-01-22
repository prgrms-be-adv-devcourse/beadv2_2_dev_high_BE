package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionCreateOrderRequestEvent(String auctionId, String productId, String buyerId,
                                             String sellerId, BigDecimal amount,
                                             OffsetDateTime orderDateTime) {


}
