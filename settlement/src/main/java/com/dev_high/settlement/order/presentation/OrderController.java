package com.dev_high.settlement.order.presentation;


import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.order.application.OrderService;
import com.dev_high.settlement.order.domain.OrderStatus;
import com.dev_high.settlement.order.presentation.dto.OrderModifyRequest;
import com.dev_high.settlement.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.settlement.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @GetMapping("{orderId}")
    public ApiResponseDto<OrderResponse> getOrderDetail(@PathVariable String orderId) {
        return orderService.findOne(orderId);
    }

    @GetMapping
    public ApiResponseDto<List<OrderResponse>> getOrders(
            @RequestParam(required = false) Optional<OrderStatus> status,
            @RequestParam(required = false) String type // "bought" or "sold"
    ) {
        return orderService.getOrders(status.orElse(null), type);
    }

    @GetMapping("/count")
    public ApiResponseDto<Long> getOrderCount(
            @RequestParam(required = false) OrderStatus status
    ) {
        return orderService.getStatusCount(status);
    }


    @PutMapping
    public ApiResponseDto<OrderResponse> update(@RequestBody OrderModifyRequest request) {
        return orderService.update(request);
    }


}
