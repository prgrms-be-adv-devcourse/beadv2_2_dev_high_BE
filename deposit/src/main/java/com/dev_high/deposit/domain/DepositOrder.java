package com.dev_high.deposit.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
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

    private static final BigDecimal MIN_ORDER_AMOUNT = BigDecimal.ONE;

    @Builder
    public DepositOrder(String userId, BigDecimal amount, DepositOrderStatus status) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
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

    public static DepositOrder create(String userId, BigDecimal amount) {
        if (amount.compareTo(MIN_ORDER_AMOUNT) < 0) {
            throw new IllegalArgumentException(
                    String.format("주문 금액은 %s원 이상이어야 합니다. (요청 금액: %s원)", MIN_ORDER_AMOUNT.toPlainString(), amount.toPlainString())
            );
        }
        return DepositOrder.builder()
                .userId(userId)
                .amount(amount)
                .status(DepositOrderStatus.PENDING) // default : PENDING
                .build();
    }

    public void updateStatus(DepositOrderStatus status) {
        this.status = status;
    }
}
