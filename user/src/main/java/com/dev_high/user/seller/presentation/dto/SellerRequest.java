package com.dev_high.user.seller.presentation.dto;

import com.dev_high.user.seller.application.dto.SellerCommand;

public record SellerRequest(
        String bankName,
        String bankAccount
) {
    public SellerCommand toCommand() {
        return new SellerCommand(bankName, bankAccount);
    }
}
