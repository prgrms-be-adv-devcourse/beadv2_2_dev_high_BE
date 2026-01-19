package com.dev_high.user.user.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "address", schema = "user")
@Getter
public class Address {

    @Id
    @Column(length = 20)
    @CustomGeneratedId(method = "address")
    private String id;

    @Column(name = "zip_code", length = 5, nullable = false)
    private String zipCode;

    @Column(name = "state", length = 25, nullable = false)
    private String state;

    @Column(name = "city", length = 20, nullable = false)
    private String city;

    @Column(name = "detail", length = 255)
    private String detail;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

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
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    protected Address() {

    }

    public Address(String zipCode, String state, String city, String detail, Boolean isDefault, String userId) {
        this.zipCode = zipCode;
        this.state = state;
        this.city = city;
        this.detail = detail;
        this.isDefault = isDefault;
        this.userId = userId;
        this.createdBy = userId;
        this.updatedBy = userId;
    }

    public void update(String zipCode, String state, String city, String detail, String updatedBy) {
        this.zipCode = zipCode;
        this.state = state;
        this.city = city;
        this.detail = detail;
        this.updatedBy = updatedBy;
    }
    public void makeDefault() {
        this.isDefault = true;
    }

    public void clearDefault() {
        this.isDefault = false;
    }

}
