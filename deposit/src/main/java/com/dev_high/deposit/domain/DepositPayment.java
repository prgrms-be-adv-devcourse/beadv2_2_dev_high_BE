package com.dev_high.deposit.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "예치금 결제")
@Table(name = "deposit_payment", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositPayment {
    @Schema(description = "예치금 결제 ID")
    @Id
    @Column(name = "id", length = 20)
    @CustomGeneratedId(method = "deposit_payment")
    private String id;

    /*
     * 추후 deposit_order 테이블의 외래 키 관계 명확화를 할수 있음
     * */
    @Schema(description = "예치금 주문 ID")
    @Column(name = "order_id", length = 20, nullable = false)
    private String orderId;

    @Schema(description = "사용자 ID")
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Schema(description = "결제키")
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Schema(description = "결제 수단")
    @Column(name = "method", nullable = false, length = 20)
    private String method;

    @Schema(description = "금액")
    @Column(name = "amount", nullable = false)
    private long amount;

    @Schema(description = "요청일시")
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Schema(description = "결제 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepositPaymentStatus status;

    @Schema(description = "승인번호")
    @Column(name = "approval_num", length = 30)
    private String approvalNum;

    @Schema(description = "승인일시")
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Schema(description = "생성일시")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "생성자")
    @Column(name = "created_by", nullable = false, updatable = false, length = 20)
    private String createdBy;

    @Schema(description = "수정일시")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Schema(description = "수정자")
    @Column(name = "updated_by", nullable = false, length = 20)
    private String updatedBy;

    @Builder
    public DepositPayment(String orderId, String userId, String paymentKey, String method, long amount, LocalDateTime requestedAt, DepositPaymentStatus status, String approvalNum, LocalDateTime approvedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentKey = paymentKey;
        this.method = method;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.status = status;
        this.approvalNum = approvalNum;
        this.approvedAt = approvedAt;
        this.createdBy = userId;
        this.updatedBy = userId;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static DepositPayment create(String orderId, String userId, long amount, String paymentKey) {
        return DepositPayment.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentKey(paymentKey)
                .method("")
                .amount(amount)
                .status(DepositPaymentStatus.READY) // default : READY
                .build();
    }

    public void confirmPayment(String paymentKey, String method, LocalDateTime approvedAt, LocalDateTime requestedAt) {
        this.paymentKey = paymentKey;
        this.status = DepositPaymentStatus.CONFIRMED;
        this.method = method;
        this.approvedAt = approvedAt;
        this.requestedAt = requestedAt;
    }

    public void failPayment() {
        this.status = DepositPaymentStatus.FAILED;
    }
}
