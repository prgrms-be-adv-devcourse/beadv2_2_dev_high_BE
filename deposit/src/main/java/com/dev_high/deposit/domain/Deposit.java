package com.dev_high.deposit.domain;

import com.dev_high.common.exception.CustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "예치금")
@Table(name = "deposit", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deposit {
    @Schema(description = "예치금 ID")
    @Id
    @Column(name = "id")
    private UUID id;

    @Schema(description = "사용자 ID")
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Schema(description = "사용가능 잔액")
    @Column(name = "balance", nullable = false)
    private long balance; // 예치금 잔액

    @Schema(description = "생성일시")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Schema(description = "생성자")
    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Schema(description = "수정일시")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Schema(description = "수정자")
    @Column(name = "updated_by", length = 20, nullable = false)
    private String updatedBy;

    @Builder
    public Deposit(UUID id, String userId, long balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.createdBy = userId;
        this.updatedBy = userId;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = userId;
    }

    public static Deposit create(String userId) {
        return Deposit.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(0L)
                .build();
    }

    public void increaseBalance(long amount) {
        this.balance += amount;
    }

    public void decreaseBalance(long amount) {
        if (this.balance < amount) {
            throw new CustomException(String.format("잔액이 부족합니다. (현재 잔액: %d, 요청 금액: %d)", this.balance, amount));
        }
        this.balance -= amount;
    }
}
