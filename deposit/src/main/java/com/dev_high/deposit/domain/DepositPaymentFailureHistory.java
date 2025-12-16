package com.dev_high.deposit.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "예치금 결제실패 이력")
@Table(name = "deposit_payment_failure_history", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositPaymentFailureHistory {
    @Schema(description = "예치금 결제 실패 이력 ID")
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * 추후 deposit_payment 테이블의 외래 키 관계 명확화를 할수 있음
     * */
    @Schema(description = "결제 ID")
    @Column(name = "deposit_payment_id", length = 20, nullable = false, updatable = false)
    private String depositPaymentId;

    @Schema(description = "사용자 ID")
    @Column(name = "user_id", length = 20, nullable = false, updatable = false)
    private String userId;

    @Schema(description = "코드")
    @Column(name = "code", nullable = false, length = 50, updatable = false)
    private String code;

    @Schema(description = "메시지")
    @Column(name = "message", nullable = false, length = 255, updatable = false)
    private String message;

    @Schema(description = "생성일시")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "생성자")
    @Column(name = "created_by", nullable = false, length = 20, updatable = false)
    private String createdBy;

    @Builder
    public DepositPaymentFailureHistory(String depositPaymentId, String userId, String code, String message) {
        this.depositPaymentId = depositPaymentId;
        this.userId = userId;
        this.code = code;
        this.message = message;
        this.createdBy = userId;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static DepositPaymentFailureHistory create(String depositPaymentId, String userId, String code, String message) {
        return DepositPaymentFailureHistory.builder()
                .depositPaymentId(depositPaymentId)
                .userId(userId)
                .code(code)
                .message(message)
                .build();
    }
}
