package com.dev_high.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "product_dtl", schema = "product")
public class ProductDtl {
    @Id
    @NotNull
    @Column(name = "id", nullable = false, length = Integer.MAX_VALUE)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private com.dev_high.product.domain.Product product;

    @NotNull
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    private String status;

    @NotNull
    @Column(name = "start_bid", nullable = false)
    private Long startBid;

    @NotNull
    @Column(name = "auction_start_at", nullable = false)
    private Instant auctionStartAt;

    @NotNull
    @Column(name = "auction_end_at", nullable = false)
    private Instant auctionEndAt;

    @Column(name = "deposit_amount")
    private Long depositAmount;

    @NotNull
    @ColumnDefault("'N'")
    @Column(name = "deleted_yn", nullable = false, length = Integer.MAX_VALUE)
    private String deletedYn;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @NotNull
    @Column(name = "created_by", nullable = false, length = Integer.MAX_VALUE)
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_by", nullable = false, length = Integer.MAX_VALUE)
    private String updatedBy;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}