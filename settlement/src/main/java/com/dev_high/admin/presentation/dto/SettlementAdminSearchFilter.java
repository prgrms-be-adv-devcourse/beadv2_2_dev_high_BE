package com.dev_high.admin.presentation.dto;

import com.dev_high.settle.domain.settle.SettlementStatus;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record SettlementAdminSearchFilter(
    String settlementId,
    String orderId,
    String sellerId,
    String buyerId,
    String auctionId,
    SettlementStatus status,
    String completeYn,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime completeFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime completeTo
) {
}
