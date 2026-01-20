package com.dev_high.deposit.order.domain.entity;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.common.type.DepositOrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Table(name = "deposit_order", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositOrder {
    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 20)
    @CustomGeneratedId(method = "deposit_order")
    private String id;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepositOrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 20)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by", nullable = false, length = 20)
    private String updatedBy;

    @Column(name = "deposit", nullable = false)
    private BigDecimal deposit;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount;


    private static final BigDecimal MIN_ORDER_AMOUNT = BigDecimal.ONE;

    @Builder
    public DepositOrder(String userId, BigDecimal amount, DepositOrderStatus status, BigDecimal deposit) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.createdBy = userId;
        this.updatedBy = userId;
        this.deposit = deposit;
        this.paidAmount = amount.subtract(deposit);
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

    public static DepositOrder create(String userId, BigDecimal amount, BigDecimal deposit) {
        if (amount.compareTo(MIN_ORDER_AMOUNT) < 0) {
            throw new IllegalArgumentException(
                    String.format("주문 금액은 %s원 이상이어야 합니다. (요청 금액: %s원)", MIN_ORDER_AMOUNT.toPlainString(), amount.toPlainString())
            );
        }

        if (deposit.compareTo(amount) > 0) {
            throw new IllegalArgumentException("사용할 예치금이 주문 금액을 초과할 수 없습니다.");
        }

        return DepositOrder.builder()
                .userId(userId)
                .amount(amount)
                .status(DepositOrderStatus.CREATED) // default : PENDING
                .deposit(deposit)
                .build();
    }

    public boolean isPayment() {
        return this.paidAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDeposit() {
        return this.deposit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCreatablePayment() {
        return this.status == DepositOrderStatus.CREATED;
    }

    public void ChangeStatus(DepositOrderStatus status) {
        this.status = status;
    }
}
