package com.dev_high.common.dto;

import java.time.OffsetDateTime;

public record WinningOrderRecommendationResponse(
    String productId,
    Long winningAmount,
    OffsetDateTime winningDate,
    String status
) {
}
