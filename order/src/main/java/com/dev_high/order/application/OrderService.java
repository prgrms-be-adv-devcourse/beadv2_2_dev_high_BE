package com.dev_high.order.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public ApiResponseDto<List<OrderResponse>> getAllOrders() {
        List<Order> found = orderRepository.findAllOrders();
        if (found.isEmpty()) return ApiResponseDto.fail("주문 없음");
        List<OrderResponse> result = found.stream().map(Order::toResponse).toList();
        return ApiResponseDto.success("주문 목록 전체 조회", result);
    }

    public ApiResponseDto<List<OrderResponse>> soldList(String sellerId) {
        List<Order> found = orderRepository.findAllOrdersBySellerId(sellerId);
        if (found.isEmpty()) return ApiResponseDto.fail("sellerId에 해당하는 주문 없음");
        List<OrderResponse> result = found.stream().map(Order::toResponse).toList();
        return ApiResponseDto.success("판매 목록 전체 조회", result);
    }

    public ApiResponseDto<List<OrderResponse>> boughtList(String buyerId) {
        List<Order> found = orderRepository.findAllOrdersByBuyerId(buyerId);
        if (found.isEmpty()) return ApiResponseDto.fail("buyerId에 해당하는 주문 없음");
        List<OrderResponse> result = found.stream().map(Order::toResponse).toList();
        return ApiResponseDto.success("구매 목록 전체 조회", result);
    }

    public ApiResponseDto<OrderResponse> findOne(String id) {
        Order found = orderRepository.findById(id).orElse(null);
        if (found == null) return ApiResponseDto.fail("id에 해당하는 주문 없음");
        return ApiResponseDto.success("주문 1건 조회", found.toResponse());
    }

    public ApiResponseDto<OrderResponse> create(OrderRegisterRequest request) {
        Order order = Order.fromRequest(request);
        Order result = orderRepository.save(order);
        return ApiResponseDto.success("주문 저장", result.toResponse());
    }

    public ApiResponseDto<OrderResponse> update(OrderModifyRequest request) {
        Order order = orderRepository.findById(request.id()).orElse(null);
        if (order == null) return ApiResponseDto.fail("id에 해당하는 주문 없음");
        order.setStatus(request.status());
        order = orderRepository.save(order);
        return ApiResponseDto.success("주문 상태 업데이트", order.toResponse());
    }

}
