package com.dev_high.user.user.domain;

import com.dev_high.common.annotation.CustomGeneratedId;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.user.application.dto.UpdateUserCommand;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
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

    @Column(name = "password", length = 500)
    private String password;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(name = "deleted_yn",  nullable = false)
    private String deletedYn;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

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
        createdBy = id;
        updatedBy = id;
        userStatus = UserStatus.ACTIVE;
        deletedYn = "N";
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedBy = id;
        updatedAt = OffsetDateTime.now();
    }

    protected User() {
    }

    public User(String email, String password, String name, String nickname, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    public User(String email, String name, String nickname, String phoneNumber, OAuthProvider provider, String providerUserId) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public void updateUser(UpdateUserCommand command) {
        this.name = command.name();
        this.nickname = command.nickname();
        this.phoneNumber = command.phone_number();
    }

    public void updatePassWord(String password) {
        this.password = password;
    }

    public void remove() {
        this.userStatus = UserStatus.WITHDRAWN;
        this.deletedAt = OffsetDateTime.now();
        this.deletedYn = "Y";
    }

    public void updateStatus(UserStatus status) {
        this.userStatus = status;
    }

    public void link(SocialProfileResponse profile) {
        this.provider = profile.provider();
        this.providerUserId = profile.providerUserId();
    }

}
