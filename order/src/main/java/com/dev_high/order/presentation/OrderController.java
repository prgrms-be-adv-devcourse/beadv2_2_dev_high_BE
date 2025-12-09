package com.dev_high.order.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.application.OrderService;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/all")
    public ApiResponseDto<List<OrderResponse>> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/sold")
    public ApiResponseDto<List<OrderResponse>> soldList(@RequestParam(name = "sellerId") String sellerId) {
        return orderService.soldList(sellerId);
    }

    @GetMapping("/bought")
    public ApiResponseDto<List<OrderResponse>> boughtList(@RequestParam(name = "buyerId") String buyerId) {
        return orderService.boughtList(buyerId);
    }


    @GetMapping("/detail")
    public ApiResponseDto<OrderResponse> detail(@RequestParam(name = "orderId") String orderId) {
        return orderService.findOne(orderId);
    }


    @PostMapping("/post")
    public ApiResponseDto<OrderResponse> create(@RequestBody OrderRegisterRequest request) {
        System.out.println("request = " + request);
        return orderService.create(request);
    }

    @PutMapping("/update")
    public ApiResponseDto<OrderResponse> update(@RequestBody OrderModifyRequest request) {
        return orderService.update(request);
    }

}
