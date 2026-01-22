package com.dev_high.admin.presentation.dto;

import com.dev_high.order.domain.OrderStatus;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record OrderAdminSearchFilter(
    String orderId,
    String sellerId,
    String buyerId,
    String auctionId,
    OrderStatus status,
    String payYn,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo
) {
}
