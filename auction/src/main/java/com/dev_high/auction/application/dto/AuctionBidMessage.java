package com.dev_high.auction.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionBidMessage(
    String type,
    String auctionId,
    String userId,
    BigDecimal bidPrice,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp,
    int currentUsers
) {}