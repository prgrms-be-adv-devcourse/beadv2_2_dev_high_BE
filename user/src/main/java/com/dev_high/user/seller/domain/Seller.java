package com.dev_high.user.seller.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
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

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    public void prePersist() {
        createdBy = user.getId();
        updatedBy = user.getId();
        this.sellerStatus = SellerStatus.ACTIVE;
        this.deletedYn = "N";
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    protected Seller() {

    }

    public Seller(User user, String bankName, String bankAccount) {
        this.user = user;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

    public void deleteSeller(SellerStatus status) {
        this.sellerStatus = status;
        this.deletedYn = "Y";
    }

    public void updateSeller(String bankName, String bankAccount) {
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }
}
