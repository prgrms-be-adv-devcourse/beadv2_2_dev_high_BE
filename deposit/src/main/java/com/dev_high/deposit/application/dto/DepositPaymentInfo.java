package com.dev_high.deposit.application.dto;

import com.dev_high.deposit.domain.DepositPayment;
import com.dev_high.deposit.domain.DepositPaymentMethod;
import com.dev_high.deposit.domain.DepositPaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/*
 * 외부에 노출되는 예치금 결제 응답(Response) DTO
 * @param id 예치금 결제 ID
 * @param orderId 예치금 주문 ID
 * @param userId 사용자 ID
 * @param paymentKey 결제키
 * @param method 결제 수단
 * @param amount 금액
 * @param requestedAt 요청일시
 * @param status 결제 상태
 * @param approvalNum 승인번호
 * @param approvedAt 승인일시
 * @param createdAt 생성일시
 * */
public record DepositPaymentInfo(
        @Schema(description = "예치금 결제 ID")
        String id,

        @Schema(description = "예치금 주문 ID")
        String orderId,

        @Schema(description = "사용자 ID")
        String userId,

        @Schema(description = "결제키")
        String paymentKey,

        @Schema(description = "결제 수단")
        String method,

        @Schema(description = "금액")
        long amount,

        @Schema(description = "요청일시")
        LocalDateTime requestedAt,

        @Schema(description = "결제 상태")
        DepositPaymentStatus status,

        @Schema(description = "승인번호")
        String approvalNum,

        @Schema(description = "승인일시")
        LocalDateTime approvedAt,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
    public static DepositPaymentInfo from(DepositPayment depositPayment) {
        return new DepositPaymentInfo(
                depositPayment.getId(),
                depositPayment.getOrderId(),
                depositPayment.getUserId(),
                depositPayment.getPaymentKey(),
                depositPayment.getMethod(),
                depositPayment.getAmount(),
                depositPayment.getRequestedAt(),
                depositPayment.getStatus(),
                depositPayment.getApprovalNum(),
                depositPayment.getApprovedAt(),
                depositPayment.getCreatedAt()
        );
    }
}
