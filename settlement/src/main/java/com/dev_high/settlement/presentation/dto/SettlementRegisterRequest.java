package com.dev_high.settlement.presentation.dto;

import com.dev_high.settlement.domain.SettlementStatus;

public record SettlementRegisterRequest(
        String id, // order의 id orderId
        String sellerId,
        String buyerId,
        String auctionId,
        Long winningAmount
) {
}
