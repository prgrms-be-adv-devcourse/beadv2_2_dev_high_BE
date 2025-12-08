package com.dev_high.user.seller.application.dto;

public record CreateSellerCommand(
        String userId,
        String bankName,
        String bankAccount
){
}
