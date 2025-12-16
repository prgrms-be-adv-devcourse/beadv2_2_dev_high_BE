package com.dev_high.deposit.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "예치금 주문")
@Table(name = "deposit_order", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositOrder {
    @Schema(description = "예치금 주문 ID")
    @Id
    @Column(name = "id", length = 20)
    @CustomGeneratedId(method = "deposit_order")
    private String id;

    @Schema(description = "사용자 ID")
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Schema(description = "금액")
    @Column(name = "amount", nullable = false)
    private long amount;

    @Schema(description = "주문 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepositOrderStatus status;

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

    private static final long MIN_ORDER_AMOUNT = 1L;

    @Builder
    public DepositOrder(String userId, long amount, DepositOrderStatus status) {
        this.userId = userId;
        this.amount = amount;
        this.status = status;
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

    public static DepositOrder create(String userId, long amount) {
        if (amount < MIN_ORDER_AMOUNT) {
            throw new IllegalArgumentException(
                    String.format("주문 금액은 %d원 이상이어야 합니다. (요청 금액: %d원)", MIN_ORDER_AMOUNT, amount)
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
