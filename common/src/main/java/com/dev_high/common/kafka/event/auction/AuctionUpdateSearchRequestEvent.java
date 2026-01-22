package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionUpdateSearchRequestEvent(String productId,
                                              String auctionId,
                                              BigDecimal startPrice,
                                              BigDecimal depositAmount,
                                              String status,
                                              OffsetDateTime auctionStartAt,
                                              OffsetDateTime auctionEndAt) {


}
