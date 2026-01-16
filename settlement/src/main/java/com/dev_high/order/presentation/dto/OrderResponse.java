package com.dev_high.order.presentation.dto;

import com.dev_high.order.application.dto.AuctionDto;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.domain.OrderStatus;


import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResponse(
        String id,
        String sellerId,
        String buyerId,
        String auctionId,
        BigDecimal winningAmount,
        BigDecimal depositAmount,
        String productName,
        OffsetDateTime confirmDate,
        OrderStatus status,
        OffsetDateTime payCompleteDate,
        OffsetDateTime payLimitDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String payYn,
        String deletedYn
) {

    public static OrderResponse fromEntity(WinningOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getSellerId(),
                order.getBuyerId(),
                order.getAuctionId(),
                order.getWinningAmount(),
                order.getDepositAmount(),
                order.getProductName(),
                order.getWinningDate(),
                order.getStatus(),
                order.getPayCompleteDate(),
                order.getPaymentLimitDate(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPayYn(),
                order.getDeletedYn()
        );
    }
}
