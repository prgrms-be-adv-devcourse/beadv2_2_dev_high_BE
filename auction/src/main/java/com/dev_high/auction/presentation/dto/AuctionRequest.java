package com.dev_high.auction.presentation.dto;

import com.dev_high.auction.domain.AuctionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AuctionRequest(String productId, BigDecimal startBid,
                             List<AuctionStatus> status,
                             OffsetDateTime auctionStartAt,
                             OffsetDateTime auctionEndAt, String sellerId , String productName) {


}
