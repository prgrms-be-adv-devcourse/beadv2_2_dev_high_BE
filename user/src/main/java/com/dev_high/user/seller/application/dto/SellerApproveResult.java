package com.dev_high.user.seller.application.dto;

import com.dev_high.user.seller.application.SellerService;

public record SellerApproveResult(int approved,
                                  int roleInserted,
                                  int skipped,
                                  int total)
{
    public static SellerApproveResult empty() {
        return new SellerApproveResult(0, 0, 0, 0);
    }
}
