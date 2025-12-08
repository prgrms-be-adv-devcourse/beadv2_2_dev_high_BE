package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;

public record AuctionCreateOrderRequestEvent(String auctionId, String productId, String buyerId,
                                             String sellerId, BigDecimal amount) {


}
