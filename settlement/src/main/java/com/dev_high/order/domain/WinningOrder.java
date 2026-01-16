package com.dev_high.order.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "winning_order", schema = "settlement") // schema/테이블명 변경
public class WinningOrder {

    @Id
    @CustomGeneratedId(method = "winning_order")
    private String id;

    @Column(name = "seller_id", nullable = false, length = 50)
    private String sellerId;

    @Column(name = "buyer_id", nullable = false, length = 50)
    private String buyerId;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "auction_id", nullable = false, length = 50)
    private String auctionId;

    @Column(name = "winning_amount", nullable = false)
    private BigDecimal winningAmount; // DB에 맞춰 통일

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount; // DB에 맞춰 통일

    @Column(name = "winning_date", nullable = false)
    private OffsetDateTime winningDate;

    @Column(name = "pay_complete_date")
    private OffsetDateTime payCompleteDate; // NULL 허용

    @Column(name = "payment_limit_date")
    private OffsetDateTime paymentLimitDate;

    @Column(nullable = false, length = 1)
    private String payYn = "N";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_yn", nullable = false)
    private String deletedYn ="N";

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 50, nullable = false)
    private String updatedBy;


    public WinningOrder(String sellerId, String buyerId, String productId, String productName, String auctionId,
                        BigDecimal winningAmount, BigDecimal depositAmount, OffsetDateTime winningDate,
                        OrderStatus status ,String creator) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.productId = productId;
        this.productName = productName;
        this.auctionId = auctionId;
        this.winningAmount = winningAmount;
        this.depositAmount = depositAmount;
        this.winningDate = winningDate;
        this.paymentLimitDate = calculatePaymentLimitDate(winningDate);
        this.status = status;
        this.createdBy=creator;
        this.updatedBy=creator;
        this.deletedYn="N";
    }

    public static WinningOrder fromRequest(OrderRegisterRequest request , String creator) {
        return new WinningOrder(
                request.sellerId(),
                request.buyerId(),
                request.productId(),
                request.productName(),
                request.auctionId(),
                request.winningAmount(),
                request.depositAmount(),
                request.winningDate(),
                OrderStatus.UNPAID,
                creator
        );
    }

    private static OffsetDateTime calculatePaymentLimitDate(OffsetDateTime winningDate) {
        OffsetDateTime base = winningDate.plusDays(3);
        return base.toLocalDate()
                .plusDays(1)
                .atStartOfDay()
                .atOffset(base.getOffset());
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(OrderStatus status, String updatedBy) {
        if (status == OrderStatus.PAID) {
            this.payYn = "Y";
            this.payCompleteDate = OffsetDateTime.now();
        }
        this.status = status;
        this.updatedBy = updatedBy;
    }


    public void changePaymentLimitDate(OffsetDateTime paymentLimitDate, String updatedBy) {
        this.paymentLimitDate = paymentLimitDate;
        this.updatedBy = updatedBy;
    }

    public void delete(String updatedBy) {
        this.deletedYn = "Y";
        this.deletedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

}
