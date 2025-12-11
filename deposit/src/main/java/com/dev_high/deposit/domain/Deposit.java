package com.dev_high.deposit.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Schema(description = "예치금")
@Table(name = "deposit", schema = "deposit")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deposit {
    @Schema(description = "사용자 ID")
    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Schema(description = "사용가능 잔액")
    @Column(name = "balance", nullable = false)
    private long balance; // 예치금 잔액

    @Schema(description = "생성일시")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Schema(description = "생성자")
    @Column(name = "created_by", length = 20, nullable = false)
    private String createdBy;

    @Schema(description = "수정일시")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Schema(description = "수정자")
    @Column(name = "updated_by", length = 20, nullable = false)
    private String updatedBy;

    @Builder
    public Deposit(String id, long balance) {
        this.id = id;
        this.balance = Optional.ofNullable(balance).orElse(0L);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.createdBy = id;
        this.updatedBy = id;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = id;
    }

    public static Deposit create(String userId) {
        return Deposit.builder()
                .id(userId)
                .balance(0L)
                .build();
    }

    public void increaseBalance(long amount) {
        this.balance += amount;
    }

    public void decreaseBalance(long amount) {
        if (this.balance < amount) {
            throw new IllegalArgumentException(String.format("잔액이 부족합니다. (현재 잔액: %d, 요청 금액: %d)", this.balance, amount));
        }
        this.balance -= amount;
    }
}
