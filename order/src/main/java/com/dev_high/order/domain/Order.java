package com.dev_high.order.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "\"order\"", schema = "\"order\"") // schema/테이블명 모두 변경
public class Order {

    @Id
    @CustomGeneratedId(method = "order")
    private String id;

    @Column(name = "seller_id", nullable = false, length = 50)
    private String sellerId;

    @Column(name = "buyer_id", nullable = false, length = 50)
    private String buyerId;

    @Column(name = "auction_id", nullable = false, length = 50)
    private String auctionId;

    @Column(name = "winning_amount", nullable = false)
    private Long winningAmount; // DB에 맞춰 통일

    @Column(name = "confirm_date", nullable = false)
    private LocalDateTime confirmDate;

    @Column(name = "pay_complete_date")
    private LocalDateTime payCompleteDate; // NULL 허용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Order(String sellerId, String buyerId, String auctionId,
                 Long winningAmount, LocalDateTime confirmDate,
                 OrderStatus status) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.auctionId = auctionId;
        this.winningAmount = winningAmount;
        this.confirmDate = confirmDate;
        this.status = status;
    }

    public static Order fromRequest(OrderRegisterRequest request) {
        return new Order(
                request.sellerId(),
                request.buyerId(),
                request.auctionId(),
                request.winningAmount(),
                request.confirmDate(),
                request.status()
        );
    }

    public OrderResponse toResponse() {
        return new OrderResponse(
                id,
                sellerId,
                buyerId,
                auctionId,
                winningAmount,
                confirmDate,
                status,
                payCompleteDate,
                createdAt,
                updatedAt
        );
    }
}
