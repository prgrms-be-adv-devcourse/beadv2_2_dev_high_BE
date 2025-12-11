package com.dev_high.settlement.config.dto;

import com.dev_high.settlement.domain.Settlement;

public record SettlementConfirmRequest(
        String settlementId,
        String orderId,
        String sellerId,
        String buyerId,
        String auctionId,
        Long winningAmount,
        Long charge,
        Long finalAmount
) {
    public static SettlementConfirmRequest fromSettlement(Settlement settlement) {
        return new SettlementConfirmRequest(
                settlement.getId(),
                settlement.getOrderId(),
                settlement.getSellerId(),
                settlement.getBuyerId(),
                settlement.getAuctionId(),
                settlement.getWinningAmount(),
                settlement.getCharge(),
                settlement.getFinalAmount()
        );
    }
}