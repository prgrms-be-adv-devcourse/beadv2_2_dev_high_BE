package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionUpdateSearchRequestEvent(String auctionId,
                                              BigDecimal startPrice,
                                              BigDecimal depositAmount,
                                              String status,
                                              LocalDateTime auctionStartAt,
                                              LocalDateTime auctionEndAt) {


}
