package com.dev_high.deposit.domain;

import com.dev_high.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table(name = "deposit", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deposit {
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "balance", nullable = false)
    private long balance; // 예치금 잔액

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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
