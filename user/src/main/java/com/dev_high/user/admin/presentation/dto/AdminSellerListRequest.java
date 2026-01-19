package com.dev_high.user.admin.presentation.dto;

public record AdminSellerListRequest(
        String userId,
        String status,
        String bankName,
        String bankAccount,
        String deletedYn
)
{
}