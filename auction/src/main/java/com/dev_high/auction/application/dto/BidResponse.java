package com.dev_high.auction.application.dto;

import java.math.BigDecimal;

public record BidResponse(BigDecimal bidPrice ,String highestUserId) {

}
