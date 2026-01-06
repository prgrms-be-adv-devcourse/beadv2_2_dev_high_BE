package com.dev_high.user.seller.domain;

import com.dev_high.user.user.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;

@Entity
@Table(name = "seller", schema = "\"user\"")
@Getter
public class Seller {

    @Id
    @Column(length = 20)
    private String id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id",nullable = false)
    private User user;

    @Column(name = "bank_name", length = 20, nullable = false)
    private String bankName;

    @Column(name = "bank_account", length = 30, nullable = false)
    private String bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private SellerStatus sellerStatus;

    @Column(name = "deleted_yn",  nullable = false)
    private String deletedYn;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_by", length = 50, nullable = false)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        createdBy = user.getId();
        updatedAt = OffsetDateTime.now();
        updatedBy = user.getId();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
        updatedBy = user.getId();
    }

    protected Seller() {

    }

    public Seller(User user, String bankName, String bankAccount) {
        this.user = user;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.deletedYn = "N";
        this.sellerStatus = SellerStatus.ACTIVE;
    }

    public void remove() {
        this.sellerStatus = SellerStatus.INACTIVE;
        this.deletedAt = OffsetDateTime.now();
        this.updatedBy = user.getId();
    }

    public void update(String bankName, String bankAccount) {
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

    public void revive(String bankName, String bankAccount) {
        this.deletedAt = null;
        this.deletedYn = "N";
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }
}
