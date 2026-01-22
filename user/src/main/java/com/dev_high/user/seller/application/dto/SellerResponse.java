package com.dev_high.user.seller.application.dto;

import com.dev_high.user.seller.domain.Seller;

public record SellerResponse(
        String userId,
        String status,
        String bankName,
        String bankAccount
) {
    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getSellerStatus().name(),
                seller.getBankName(),
                seller.getBankAccount()
        );
    }
}
