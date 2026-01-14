package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionCreateOrderRequestEvent(String auctionId,
                                             String productId,
                                             String productName,
                                             String buyerId,
                                             String sellerId,
                                             BigDecimal amount,
                                             BigDecimal depositAmount,
                                             OffsetDateTime orderDateTime) {


}
