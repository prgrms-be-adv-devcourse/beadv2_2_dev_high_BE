package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;

public record AuctionUpdateSearchRequestEvent(String auctionId,
                                              BigDecimal startPrice, BigDecimal depositAmount,
                                              String status) {


}
