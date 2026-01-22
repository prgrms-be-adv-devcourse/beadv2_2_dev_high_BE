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
        Long winningAmount,
        BigDecimal depositAmount,
        String productName,
        OffsetDateTime confirmDate,
        OrderStatus status,
        OffsetDateTime payCompleteDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String payYn
) {

    public static OrderResponse fromEntity(WinningOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getSellerId(),
                order.getBuyerId(),
                order.getAuctionId(),
                order.getWinningAmount(),
                null,
                null,
                order.getWinningDate(),
                order.getStatus(),
                order.getPayCompleteDate(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPayYn()
        );
    }

    public static OrderResponse fromEntity(WinningOrder order, AuctionDto auctionDto) {
        return new OrderResponse(
                order.getId(),
                order.getSellerId(),
                order.getBuyerId(),
                order.getAuctionId(),
                order.getWinningAmount(),
                auctionDto.depositAmount(),
                auctionDto.productName(),
                order.getWinningDate(),
                order.getStatus(),
                order.getPayCompleteDate(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPayYn()
        );
    }
}
