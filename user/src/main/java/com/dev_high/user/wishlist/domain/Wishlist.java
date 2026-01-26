package com.dev_high.user.wishlist.domain;


import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.user.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wishlist", schema = "user")
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

    @Column(name = "deleted_yn", nullable = false)
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
        updatedBy = user.getId();
        updatedAt = OffsetDateTime.now();
    }

    protected Wishlist() {

    }

    public Wishlist(User user, String productId) {
        this.user = user;
        this.productId = productId;
        this.deletedYn = "N";
    }

    public void remove() {
        this.deletedYn = "Y";
        this.deletedAt = OffsetDateTime.now();
    }

    public void restore() {
        this.deletedYn = "N";
        this.deletedAt = null;
    }
}
