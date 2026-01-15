package com.dev_high.deposit.payment.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Table(name = "deposit_payment_failure_history", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositPaymentFailureHistory {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /*
     * 추후 deposit_order 테이블의 외래 키 관계 명확화를 할수 있음
     * */
    @Column(name = "order_id", length = 20, nullable = false, updatable = false)
    private String orderId;

    @Column(name = "user_id", length = 20, nullable = false, updatable = false)
    private String userId;

    @Column(name = "code", nullable = false, length = 50, updatable = false)
    private String code;

    @Column(name = "message", nullable = false, length = 255, updatable = false)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 20, updatable = false)
    private String createdBy;

    @Builder
    public DepositPaymentFailureHistory(String orderId, String userId, String code, String message) {
        this.orderId = orderId;
        this.userId = userId;
        this.code = code;
        this.message = message;
        this.createdBy = userId;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    public static DepositPaymentFailureHistory create(String orderId, String userId, String code, String message) {
        return DepositPaymentFailureHistory.builder()
                .orderId(orderId)
                .userId(userId)
                .code(code)
                .message(message)
                .build();
    }
}
