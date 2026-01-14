package com.dev_high.settle.domain.history;


import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@NoArgsConstructor
@Table(name = "settlement_history", schema = "settlement")
public class SettlementHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "settlement_id", length = 50, nullable = false)
  private String settlementId;

  @Column(name = "seller_id", length = 50, nullable = false)
  private String sellerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private SettlementStatus status;

  @Column(name = "message")
  private String message;

  @Column(name = "created_by", length = 50)
  private String createdBy = "SYSTEM";

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  public SettlementHistory(String settlementId, String sellerId, SettlementStatus status,
      String message) {
    this.settlementId = settlementId;
    this.sellerId = sellerId;
    this.status = status;
    this.message = message;
  }

  public static SettlementHistory fromSettlement(Settlement settlement) {
    String message = settlement.getHistoryMessage();
    if (message == null) {
      message = String.format("%s %s %s", settlement.getSellerId(), settlement.getStatus(),
          settlement.getUpdateDate());
    }

    return new SettlementHistory(
        settlement.getId(),
        settlement.getSellerId(),
        settlement.getStatus(),
        message);
  }

}