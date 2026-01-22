package com.dev_high.order.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "order", schema = "order") // schema/테이블명 모두 변경
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

    @Column(name = "winning_date", nullable = false)
    private OffsetDateTime winningDate;

    @Column(name = "pay_complete_date")
    private OffsetDateTime payCompleteDate; // NULL 허용

    @Column(nullable = false, length = 1)
    private String payYn = "N";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Order(String sellerId, String buyerId, String auctionId,
                 Long winningAmount, OffsetDateTime winningDate,
                 OrderStatus status) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.auctionId = auctionId;
        this.winningAmount = winningAmount;
        this.winningDate = winningDate;
        this.status = status;
    }

    public static Order fromRequest(OrderRegisterRequest request) {
        return new Order(
                request.sellerId(),
                request.buyerId(),
                request.auctionId(),
                request.winningAmount(),
                request.winningDate(),
                request.status()
        );
    }


}
