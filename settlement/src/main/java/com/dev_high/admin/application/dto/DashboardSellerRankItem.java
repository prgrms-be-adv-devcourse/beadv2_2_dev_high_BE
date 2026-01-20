package com.dev_high.admin.application.dto;

import java.math.BigDecimal;

public record DashboardSellerRankItem(
    String sellerId,
    String sellerName,
    BigDecimal gmv
) {
}
