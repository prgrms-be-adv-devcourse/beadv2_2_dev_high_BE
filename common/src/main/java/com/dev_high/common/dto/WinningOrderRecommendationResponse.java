package com.dev_high.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WinningOrderRecommendationResponse(
    String productId,
    BigDecimal winningAmount,
    OffsetDateTime winningDate,
    String status
) {
}
