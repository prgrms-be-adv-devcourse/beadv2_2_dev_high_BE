package com.dev_high.settlement.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import com.dev_high.settlement.presentation.dto.SettlementResponse;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "settlement", schema = "settlement")
public class Settlement {

    private final static double chargeRatio = 0.3;

    @Id
    @CustomGeneratedId(method = "settlement")
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @Column(name = "auction_id", nullable = false)
    private String auctionId;

    @Column(name = "winning_amount", nullable = false)
    private Long winningAmount;

    @Column(name = "charge")
    private Long charge;

    @Column(name = "final_amount")
    private Long finalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "complete_date")
    private LocalDateTime completeDate;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "complete_yn", nullable = false, length = 1)
    private String completeYn ="N";

    public Settlement(String orderId, String sellerId, String buyerId, String auctionId, Long winningAmount, LocalDateTime dueDate, SettlementStatus status) {
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.auctionId = auctionId;
        this.winningAmount = winningAmount;
        this.status = status;
        this.dueDate = dueDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    public Settlement makeComplete() {
        this.completeYn = "Y";
        this.charge = (long) (winningAmount * chargeRatio);
        this.finalAmount = winningAmount - charge;
        this.status = SettlementStatus.COMPLETED;
        this.completeDate = LocalDateTime.now();

        return this;
    }

    public static Settlement fromRequest(SettlementRegisterRequest request) {
        return new Settlement(
                request.id(),
                request.sellerId(),
                request.buyerId(),
                request.auctionId(),
                request.winningAmount(),
                LocalDate.now().plusMonths(1).withDayOfMonth(3).atStartOfDay(),
                SettlementStatus.WAITING
        );
    }

    public SettlementResponse toResponse() {
        return new SettlementResponse(
                id,
                orderId,
                sellerId,
                buyerId,
                auctionId,
                winningAmount,
                charge,
                finalAmount,
                dueDate,
                status,
                completeYn,
                createdAt,
                completeDate,
                updateDate
        );
    }
}