package com.dev_high.user.seller.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.user.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "seller", schema = "\"user\"")
@Getter
public class Seller {

    @Id
    @Column(length = 20)
    // db 테이블명을 넣어줌 > public.idgenerator_meta 테이블에 정보 등록되어있어야함.
    @CustomGeneratedId(method = "seller")
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
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
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        createdBy = user.getId();
        updatedBy = user.getId();
        this.sellerStatus = SellerStatus.ACTIVE;
        this.deletedYn = "N";
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    protected Seller() {

    }

    public Seller(User user, String bankName, String bankAccount) {
        this.user = user;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

    public void deleteSeller() {
        this.sellerStatus = SellerStatus.INACTIVE;
        this.deletedYn = "Y";
    }

    public void updateSeller(String bankName, String bankAccount) {
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }
}
