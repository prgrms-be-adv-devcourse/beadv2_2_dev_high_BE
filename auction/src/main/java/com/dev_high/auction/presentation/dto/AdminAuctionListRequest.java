package com.dev_high.auction.presentation.dto;

import com.dev_high.auction.domain.AuctionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminAuctionListRequest(
    AuctionStatus status,
    String deletedYn,
    String productId,
    String sellerId,
    BigDecimal minBid,
    BigDecimal maxBid,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTo,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTo
) {}
