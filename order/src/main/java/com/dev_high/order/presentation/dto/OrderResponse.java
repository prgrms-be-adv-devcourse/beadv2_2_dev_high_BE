package com.dev_high.order.presentation.dto;

import com.dev_high.order.application.dto.AuctionDto;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        String id,
        String sellerId,
        String buyerId,
        String auctionId,
        Long winningAmount,
        BigDecimal depositAmount,
        String productName,
        LocalDateTime confirmDate,
        OrderStatus status,
        LocalDateTime payCompleteDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String payYn
) {

    public static OrderResponse fromEntity(Order order) {
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

    public static OrderResponse fromEntity(Order order, AuctionDto auctionDto) {
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