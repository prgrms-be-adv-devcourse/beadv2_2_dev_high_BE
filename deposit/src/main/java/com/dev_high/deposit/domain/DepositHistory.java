package com.dev_high.deposit.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "예치금 이력")
@Table(name = "deposit_history", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositHistory {
    @Schema(description = "예치금 이력 ID")
    @Id
    @Column(name = "id")
    private Long id;

    /*
    * 추후 deposit 테이블의 외래 키 관계 명확화를 할수 있음
    * */
    @Schema(description = "사용자 ID")
    @Column(name = "user_id", length = 20, nullable = false, updatable = false)
    private String userId;

    /*
     * 추후 deposit_order 테이블의 외래 키 관계 명확화를 할수 있음
     * */
    @Schema(description = "예치금 주문 ID")
    @Column(name = "deposit_order_id", length = 20, nullable = false, updatable = false)
    private String depositOrderId;

    @Schema(description = "예치금 유형")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20, updatable = false)
    private DepositType type;

    @Schema(description = "금액")
    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Schema(description = "현재 예치금 잔액")
    @Column(name = "balance", nullable = false, updatable = false)
    private long balance;

    @Schema(description = "생성일시")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "생성자")
    @Column(name = "created_by", nullable = false, length = 20, updatable = false)
    private String createdBy;

    @Builder
    public DepositHistory(String userId,  String depositOrderId, DepositType type, long amount, long balance) {
        this.userId = userId;
        this.depositOrderId = depositOrderId;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static DepositHistory create(String userId, String depositOrderId, DepositType type, long amount, long currentBalance) {
        return DepositHistory.builder()
                .userId(userId)
                .depositOrderId(depositOrderId)
                .type(type)
                .amount(amount)
                .balance(currentBalance)
                .build();
    }
}
