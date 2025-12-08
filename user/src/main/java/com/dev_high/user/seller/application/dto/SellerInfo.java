package com.dev_high.user.seller.application.dto;

import com.dev_high.user.seller.domain.Seller;

public record SellerInfo(
        String userId,
        String bankName,
        String bankAccount
) {
    public static SellerInfo from(Seller seller) {
        return new SellerInfo(
                seller.getUser().getId(),
                seller.getBankName(),
                seller.getBankAccount()
        );
    }
}
