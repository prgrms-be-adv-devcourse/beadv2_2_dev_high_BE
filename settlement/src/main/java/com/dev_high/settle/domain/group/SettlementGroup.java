package com.dev_high.settle.domain.group;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.settle.presentation.dto.SettlementGroupResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(
    name = "settlement_group",
    schema = "settlement",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_settlement_group_seller_date",
            columnNames = {"seller_id", "settlement_date"}
        )
    }
)
public class SettlementGroup {

  @Id
  @CustomGeneratedId(method = "settlement_group")
  private String id;

  @Column(name = "seller_id", nullable = false)
  private String sellerId;

  @Column(name = "settlement_date", nullable = false)
  private LocalDate settlementDate;

  @Column(name = "total_charge")
  private BigDecimal totalCharge;

  @Column(name = "total_final_amount")
  private BigDecimal totalFinalAmount;

  @Column(name = "paid_charge")
  private BigDecimal paidCharge;

  @Column(name = "paid_final_amount")
  private BigDecimal paidFinalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "deposit_status", nullable = false, length = 20)
  private DepositStatus depositStatus = DepositStatus.PENDING;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "update_date", nullable = false)
  private OffsetDateTime updateDate;

  public SettlementGroup(String sellerId, LocalDate settlementDate) {
    this.sellerId = sellerId;
    this.settlementDate = settlementDate;
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

  public void addTotals(BigDecimal charge, BigDecimal finalAmount) {
    BigDecimal currentCharge = normalize(this.totalCharge);
    BigDecimal currentFinal = normalize(this.totalFinalAmount);
    BigDecimal addCharge = normalize(charge);
    BigDecimal addFinal = normalize(finalAmount);
    this.totalCharge = currentCharge.add(addCharge);
    this.totalFinalAmount = currentFinal.add(addFinal);
  }

  public void addPaid(BigDecimal charge, BigDecimal finalAmount) {
    BigDecimal currentCharge = normalize(this.paidCharge);
    BigDecimal currentFinal = normalize(this.paidFinalAmount);
    BigDecimal addCharge = normalize(charge);
    BigDecimal addFinal = normalize(finalAmount);
    this.paidCharge = currentCharge.add(addCharge);
    this.paidFinalAmount = currentFinal.add(addFinal);
  }

  public void refreshDepositStatus() {
    BigDecimal totalFinal = normalize(this.totalFinalAmount);
    BigDecimal paidFinal = normalize(this.paidFinalAmount);
    if (totalFinal.compareTo(BigDecimal.ZERO) <= 0 || paidFinal.compareTo(BigDecimal.ZERO) <= 0) {
      this.depositStatus = DepositStatus.PENDING;
      return;
    }
    if (paidFinal.compareTo(totalFinal) >= 0) {
      this.depositStatus = DepositStatus.SUCCESS;
    } else {
      this.depositStatus = DepositStatus.PARTIAL;
    }
  }

  public SettlementGroupResponse toResponse() {
    return new SettlementGroupResponse(
        id,
        sellerId,
        settlementDate,
        totalCharge,
        totalFinalAmount,
        paidCharge,
        paidFinalAmount,
        depositStatus,
        createdAt,
        updateDate
    );
  }

  private BigDecimal normalize(BigDecimal amount) {
    if (amount == null) {
      return BigDecimal.ZERO;
    }
    return amount.setScale(0, RoundingMode.DOWN);
  }
}
