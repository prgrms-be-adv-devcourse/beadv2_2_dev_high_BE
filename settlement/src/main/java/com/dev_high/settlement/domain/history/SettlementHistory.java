package com.dev_high.settlement.domain.history;


import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SettlementHistory(String settlementId, String sellerId, SettlementStatus status, String message) {
        this.settlementId = settlementId;
        this.sellerId = sellerId;
        this.status = status;
        this.message = message;
    }

    public static SettlementHistory fromSettlement(Settlement settlement) {
        return new SettlementHistory(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getStatus(),
                settlement.getSellerId() + settlement.getStatus() + settlement.getUpdateDate()
        );
    }

}