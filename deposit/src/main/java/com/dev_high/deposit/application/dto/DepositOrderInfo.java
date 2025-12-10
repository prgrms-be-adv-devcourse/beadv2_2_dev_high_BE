package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositOrder;
import com.dev_high.deposit.domain.DepositOrderStatus;

import java.time.LocalDateTime;

/*
 * 외부에 노출되는 예치금 주문 응답(Response) DTO
 * @param orderId 예치금 주문 ID
 * @param userId 예치금 사용자 ID
 * @param amount 주문 금액
 * @param status 주문 상태
 * @param createdAt 생성일시
 * */
public record DepositOrderInfo(
        String orderId,
        String userId,
        long amount,
        DepositOrderStatus status,
        LocalDateTime createdAt
) {
    public static DepositOrderInfo from(DepositOrder depositOrder) {
        return new DepositOrderInfo(
                depositOrder.getId(),
                depositOrder.getUserId(),
                depositOrder.getAmount(),
                depositOrder.getStatus(),
                depositOrder.getCreatedAt()
        );
    }
}
