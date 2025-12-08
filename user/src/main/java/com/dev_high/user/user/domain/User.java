package com.dev_high.user.user.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Table(name = "\"user\"", schema = "\"user\"")
@Getter
public class User {

    @Id
    @Column(length = 20)
    // db 테이블명을 넣어줌 > public.idgenerator_meta 테이블에 정보 등록되어있어야함.
    @CustomGeneratedId(method = "user")
    private String id;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "password", length = 500, nullable = false)
    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "zip_code", length = 5, nullable = false)
    private String zipCode;

    @Column(name = "state", length = 25, nullable = false)
    private String state;

    @Column(name = "city", length = 20, nullable = false)
    private String city;

    @Column(name = "detail", length = 255, nullable = false)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private UserStatus userStatus;

    @Column(name = "deleted_yn",  nullable = false)
    private String deletedYn;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

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
        createdBy = id;
        updatedBy = id;
        this.userStatus = UserStatus.PENDING_VERIFICATION;
        this.deletedYn = "N";
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


    protected User() {
    }

    public User(String email, String password, String name, String nickname, String phoneNumber, String zipCode, String state, String city, String detail) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.zipCode = zipCode;
        this.state = state;
        this.city = city;
        this.detail = detail;
    }
}
