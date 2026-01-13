package com.dev_high.auction.presentation.dto;

import com.dev_high.auction.domain.AuctionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record AdminAuctionListRequest(
    AuctionStatus status,
    String deletedYn,
    String productId,
    String sellerId,
    BigDecimal minBid,
    BigDecimal maxBid,
    @Schema(description = "경매 시작일 From (예: 2025-12-12 00:00:00)", example = "2025-12-12 00:00:00")
    String startFrom,
    @Schema(description = "경매 시작일 To (예: 2025-12-12 23:59:59)", example = "2025-12-12 23:59:59")
    String startTo,
    @Schema(description = "경매 종료일 From (예: 2025-12-12 00:00:00)", example = "2025-12-12 00:00:00")
    String endFrom,
    @Schema(description = "경매 종료일 To (예: 2025-12-12 23:59:59)", example = "2025-12-12 23:59:59")
    String endTo) {}
