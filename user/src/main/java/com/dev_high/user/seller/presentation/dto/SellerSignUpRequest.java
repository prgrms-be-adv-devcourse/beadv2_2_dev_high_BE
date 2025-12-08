package com.dev_high.user.seller.presentation.dto;

import com.dev_high.user.seller.application.dto.CreateSellerCommand;

public record SellerSignUpRequest(
        String userId,
        String bankName,
        String bankAccount
) {
    public CreateSellerCommand toCommand() {
        return new CreateSellerCommand(userId, bankName, bankAccount);
    }
}
