package com.dev_high.user.seller.admin.presentation.dto;

import java.util.List;

public record SellerApproveRequest(
        List<String> sellerIds
) {
}
