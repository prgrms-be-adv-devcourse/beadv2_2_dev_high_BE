package com.dev_high.user.wishlist.domain;


import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.user.seller.domain.SellerStatus;
import com.dev_high.user.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist", schema = "\"user\"")
@Getter
public class Wishlist {

    @Id
    @Column(length = 20)
    // db 테이블명을 넣어줌 > public.idgenerator_meta 테이블에 정보 등록되어있어야함.
    @CustomGeneratedId(method = "wishlist")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "product_id", length = 20, nullable = false)
    private String productId;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        createdBy = user.getId();
        createdAt = LocalDateTime.now();
    }

    protected Wishlist() {

    }

    public Wishlist(User user, String productId) {
        this.user = user;
        this.productId = productId;
    }
}
