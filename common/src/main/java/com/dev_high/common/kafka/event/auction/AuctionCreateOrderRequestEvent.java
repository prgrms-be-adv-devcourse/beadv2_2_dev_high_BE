package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionCreateOrderRequestEvent(String auctionId, String productId, String buyerId,
                                             String sellerId, BigDecimal amount,
                                             LocalDateTime orderDateTime) {


}
