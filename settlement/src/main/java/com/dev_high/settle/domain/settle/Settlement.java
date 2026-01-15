package com.dev_high.settle.domain.settle;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 정산 정보를 나타내는 엔티티 클래스
 */
@Getter
@Entity
@NoArgsConstructor
@Table(name = "settlement", schema = "settlement")
public class Settlement {


    private final static double chargeRatio = 0.05;

    /**
     * 정산 ID (커스텀 생성)
     */
    @Id
    @CustomGeneratedId(method = "settlement")
    private String id;

    /**
     * 주문 ID
     */
    @Column(name = "order_id", nullable = false)
    private String orderId;

    /**
     * 판매자 ID
     */
    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    /**
     * 구매자 ID
     */
    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    /**
     * 경매 ID
     */
    @Column(name = "auction_id", nullable = false)
    private String auctionId;

    /**
     * 낙찰 금액
     */
    @Column(name = "winning_amount", nullable = false)
    private BigDecimal winningAmount;

    /**
     * 수수료
     */
    @Column(name = "charge")
    private BigDecimal charge;

    /**
     * 최종 정산 금액
     */
    @Column(name = "final_amount")
    private BigDecimal finalAmount;


    /**
     * 정산 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementStatus status;

    /**
     * 생성일
     */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * 정산 완료일
     */
    @Column(name = "complete_date")
    private OffsetDateTime completeDate;

    /**
     * 수정일
     */
    @Column(name = "update_date", nullable = false)
    private OffsetDateTime updateDate;

    /**
     * 완료 여부 (Y/N)
     */
    @Column(name = "complete_yn", nullable = false, length = 1)
    private String completeYn = "N";

    /**
     * 시도 횟수
     */
    @Column(name = "try_cnt", nullable = false, length = 1)
    private Long tryCnt;
    @Transient
    private String historyMessage;

    public Settlement(String orderId, String sellerId, String buyerId, String auctionId,
                      BigDecimal winningAmount, SettlementStatus status, Long tryCnt) {

        this.orderId = orderId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.auctionId = auctionId;
        this.winningAmount = winningAmount;
        this.status = status;
        this.tryCnt = tryCnt;

    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updateDate = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = OffsetDateTime.now();
    }

    /**
     * 정산 상태를 업데이트합니다. 상태가 COMPLETED로 변경되면 완료 여부와 완료일을 설정합니다. 상태가 FAILED로 변경되면 재시도를 위해 ready 메소드를
     * 호출합니다.
     *
     * @param status 새로운 정산 상태
     */
    public void updateStatus(SettlementStatus status) {
        this.status = status;

        if (status == SettlementStatus.COMPLETED) {
            this.completeYn = "Y";
            this.completeDate = OffsetDateTime.now();
        }
    }

    public void ready() {

        if (this.tryCnt == 0) {
            this.charge = winningAmount.multiply(BigDecimal.valueOf(chargeRatio));
            this.finalAmount = winningAmount.subtract(charge);
        }
        this.tryCnt++;
    }

    public void setHistoryMessage(String historyMessage) {
        this.historyMessage = historyMessage;
    }

    /**
     * Settlement 엔티티를 SettlementResponse DTO로 변환합니다.
     *
     * @return 변환된 SettlementResponse DTO
     */
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
                status,
                completeYn,
                createdAt,
                completeDate,
                updateDate
        );
    }
}
