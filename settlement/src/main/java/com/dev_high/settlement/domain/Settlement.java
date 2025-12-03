package com.dev_high.settlement.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import com.dev_high.settlement.presentation.dto.SettlementResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
  private Long winningAmount;

  /**
   * 수수료
   */
  @Column(name = "charge")
  private Long charge;

  /**
   * 최종 정산 금액
   */
  @Column(name = "final_amount")
  private Long finalAmount;

  /**
   * 정산 예정일
   */
  @Column(name = "due_date", nullable = false)
  private LocalDateTime dueDate;

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
  private LocalDateTime createdAt;

  /**
   * 정산 완료일
   */
  @Column(name = "complete_date")
  private LocalDateTime completeDate;

  /**
   * 수정일
   */
  @Column(name = "update_date", nullable = false)
  private LocalDateTime updateDate;

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

  public Settlement(String orderId, String sellerId, String buyerId, String auctionId,
      Long winningAmount, SettlementStatus status, Long tryCnt, LocalDateTime dueDate) {

    this.orderId = orderId;
    this.sellerId = sellerId;
    this.buyerId = buyerId;
    this.auctionId = auctionId;
    this.winningAmount = winningAmount;
    this.status = status;
    this.dueDate = dueDate;
    this.tryCnt = tryCnt;

  }

  public Settlement(SettlementRegisterRequest request, LocalDateTime dueDate) {
    this(request.id(), request.sellerId(), request.buyerId(), request.auctionId(),
        request.winningAmount(), SettlementStatus.WAITING, 0L, dueDate);

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
      this.completeDate = LocalDateTime.now();
    }
  }

  public void ready() {

    if (this.tryCnt == 0) {
      this.charge = (long) (winningAmount * chargeRatio);
      this.finalAmount = winningAmount - charge;
    }
    this.tryCnt++;
  }


  @Transient
  private String historyMessage;

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
        dueDate,
        status,
        completeYn,
        createdAt,
        completeDate,
        updateDate
    );
  }
}