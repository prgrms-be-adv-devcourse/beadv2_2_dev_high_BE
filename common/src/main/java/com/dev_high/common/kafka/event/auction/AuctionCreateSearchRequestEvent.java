package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionCreateSearchRequestEvent(String auctionId, String productId,
                                              String productName,
                                              List<String> categories, String description,
                                              BigDecimal startPrice, BigDecimal depositAmount,
                                              String status, String sellerId,
                                              LocalDateTime auctionStartAt,
                                              LocalDateTime auctionEndAt) {


}
