package com.dev_high.user.admin.service.dto;

import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerStatus;
import com.dev_high.user.user.domain.OAuthProvider;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserStatus;
import com.querydsl.core.annotations.QueryProjection;

import java.time.OffsetDateTime;

public record UserDetailResponse(
        String id,
        String email,
        String password,
        String name,
        String nickname,
        String phoneNumber,
        UserStatus userStatus,
        OAuthProvider provider,
        String deletedYn,
        OffsetDateTime deletedAt,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt,
        SellerStatus sellerStatus,
        String bankAccount,
        String bankName

) {

    @QueryProjection
    public UserDetailResponse {
    }

    public UserDetailResponse(User user, Seller seller) {
        this(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getUserStatus(),
                user.getProvider(),
                user.getDeletedYn(),
                user.getDeletedAt(),
                user.getCreatedBy(),
                user.getCreatedAt(),
                user.getUpdatedBy(),
                user.getUpdatedAt(),

                seller.getSellerStatus(),
                seller.getBankAccount(),
                seller.getBankName()
        );
    }
}
