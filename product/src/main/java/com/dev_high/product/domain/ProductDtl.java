package com.dev_high.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "product_dtl", schema = "product")
public class ProductDtl {
    public static final String DEFAULT_STATUS = "READY";
    public static final String NOT_DELETED = "N";

    @Id
    @NotNull
    @Column(name = "id", nullable = false, length = Integer.MAX_VALUE)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    private String status;

    @NotNull
    @Column(name = "start_bid", nullable = false)
    private Long startBid;

    @NotNull
    @Column(name = "auction_start_at", nullable = false)
    private OffsetDateTime auctionStartAt;

    @NotNull
    @Column(name = "auction_end_at", nullable = false)
    private OffsetDateTime auctionEndAt;

    @Column(name = "deposit_amount")
    private Long depositAmount;

    @NotNull
    @ColumnDefault("'N'")
    @Column(name = "deleted_yn", nullable = false, length = Integer.MAX_VALUE)
    private String deletedYn;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @NotNull
    @Column(name = "created_by", nullable = false, length = Integer.MAX_VALUE)
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @Column(name = "updated_by", nullable = false, length = Integer.MAX_VALUE)
    private String updatedBy;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ProductDtl() {
    }

    private ProductDtl(Product product,
                       String status,
                       Long startBid,
                       OffsetDateTime auctionStartAt,
                       OffsetDateTime auctionEndAt,
                       String createdBy) {
        this.id = product.getId();
        this.product = product;
        this.status = status;
        this.startBid = startBid;
        this.auctionStartAt = auctionStartAt;
        this.auctionEndAt = auctionEndAt;
        this.deletedYn = NOT_DELETED;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ProductDtl create(Product product,
                                    String status,
                                    Long startBid,
                                    OffsetDateTime auctionStartAt,
                                    OffsetDateTime auctionEndAt,
                                    String createdBy) {
        return new ProductDtl(product, status, startBid, auctionStartAt, auctionEndAt, createdBy);
    }

    public void updateDetails(Long startBid,
                              OffsetDateTime auctionStartAt,
                              OffsetDateTime auctionEndAt,
                              String updatedBy) {
        this.startBid = startBid;
        this.auctionStartAt = auctionStartAt;
        this.auctionEndAt = auctionEndAt;
        this.updatedBy = updatedBy;
        this.updatedAt = OffsetDateTime.now();
    }
}
