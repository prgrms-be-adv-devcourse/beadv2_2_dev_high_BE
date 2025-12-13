package com.dev_high.auction.presentation.dto;

import com.dev_high.auction.domain.AuctionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

public record AuctionRequest(String productId, BigDecimal startBid,
                             List<AuctionStatus> status,
                             @Schema(description = "경매 시작 시간, 시간 단위 입력 가능 (예: 2025-12-12 11:00:00)",
                                 example = "2025-12-12 11:00:00")
                             String auctionStartAt,
                             @Schema(description = "경매 종료 시간, 시간 단위 입력 가능 (예: 2025-12-12 15:00:00)",
                                 example = "2025-12-12 15:00:00")
                             String auctionEndAt, String sellerId) {


}
