package com.dev_high.common.kafka.event.auction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AuctionUpdateSearchRequestEvent(String auctionId, String productId,
                                              String productName,
                                              List<String> categories, String description,
                                              BigDecimal startPrice, BigDecimal depositAmount,
                                              String status, String sellerId,
                                              OffsetDateTime auctionStartAt,
                                              OffsetDateTime auctionEndAt) {


}
