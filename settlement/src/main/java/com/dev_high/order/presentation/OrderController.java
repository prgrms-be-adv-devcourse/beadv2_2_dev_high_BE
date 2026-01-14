package com.dev_high.order.presentation;


import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.application.OrderService;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import com.dev_high.common.dto.WinningOrderRecommendationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "WinningOrder", description = "낙찰주문 관리 API")
public class OrderController {

    private final OrderService orderService;


    @GetMapping("{orderId}")
    public ApiResponseDto<OrderResponse> getOrderDetail(@PathVariable String orderId) {
        return ApiResponseDto.success(orderService.findOne(orderId));
    }

    @GetMapping
    public ApiResponseDto<Page<OrderResponse>> getOrders(
            @RequestParam(required = false) Optional<OrderStatus> status,
            @RequestParam(required = false) String type, // "bought" or "sold"
            Pageable pageable
    ) {
        return ApiResponseDto.success(orderService.getOrders(status.orElse(null), type,pageable));
    }

    @GetMapping("/count")
    public ApiResponseDto<Long> getOrderCount(
            @RequestParam(required = false) OrderStatus status
    ) {
        return ApiResponseDto.success(orderService.getStatusCount(status));
    }


    @PutMapping
    public ApiResponseDto<OrderResponse> update(@RequestBody OrderModifyRequest request) {
        return ApiResponseDto.success(orderService.update(request));
    }

    @GetMapping("/winning/recommendation")
    public ApiResponseDto<List<WinningOrderRecommendationResponse>> getWinningOrdersForRecommendation(
        @RequestParam List<String> productIds,
        @RequestParam(defaultValue = "200") int limit,
        @RequestParam(defaultValue = "180") int days
    ) {
        return ApiResponseDto.success(orderService.getWinningOrdersForRecommendation(productIds, limit, days));
    }

}
