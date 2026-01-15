package com.dev_high.common.kafka.event.deposit;

import java.math.BigDecimal;
import java.util.List;

public record DepositCompletedEvent(
        List<String> userIds,       // 보증금 지불 대상 유저
        String auctionId,     // 경매 ID
        BigDecimal amount,     // 보증금 금액
        String type
) {
    public static DepositCompletedEvent of(List<String> userIds, String auctionId, BigDecimal amount, String type) {
        return new DepositCompletedEvent(userIds, auctionId, amount, type);
    }
}
