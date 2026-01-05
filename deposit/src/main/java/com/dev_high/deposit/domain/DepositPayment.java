package com.dev_high.deposit.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Table(name = "deposit_payment", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositPayment {
    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 20)
    @CustomGeneratedId(method = "deposit_payment")
    private String id;

    /*
     * 추후 deposit_order 테이블의 외래 키 관계 명확화를 할수 있음
     * */
    @Column(name = "order_id", length = 20, nullable = false)
    private String orderId;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "method", nullable = false, length = 20)
    private String method;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "requested_at")
    private OffsetDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepositPaymentStatus status;

    @Column(name = "approval_num", length = 30)
    private String approvalNum;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 20)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false, length = 20)
    private String updatedBy;

    @Builder
    public DepositPayment(String orderId, String userId, String paymentKey, String method, long amount, OffsetDateTime requestedAt, DepositPaymentStatus status, String approvalNum, OffsetDateTime approvedAt) {
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
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
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

    public void confirmPayment(String paymentKey, String method, OffsetDateTime approvedAt, OffsetDateTime requestedAt) {
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
