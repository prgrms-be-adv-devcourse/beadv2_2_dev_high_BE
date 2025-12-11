package com.dev_high.order.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.application.OrderService;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

//    @GetMapping("/all")
//    public ApiResponseDto<List<OrderResponse>> getAllOrders() {
//        return orderService.getAllOrders();
//    }

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

    @GetMapping("/findConfirmed")
    public List<OrderResponse> findConfirmed() {
        return orderService.findConfirmedOrders(OrderStatus.CONFIRM_BUY,
                LocalDate.now().minusWeeks(2).atStartOfDay(),
                LocalDate.now().minusWeeks(2).atTime(LocalTime.MAX));
    }


    @PostMapping("/post")
    public ApiResponseDto<OrderResponse> create(@RequestBody OrderRegisterRequest request) {
        String validateResult = validateRegisterParam(request);
        if (validateResult != null) return ApiResponseDto.fail(validateResult);
        return orderService.create(request);
    }

    @PutMapping("/update")
    public ApiResponseDto<OrderResponse> update(@RequestBody OrderModifyRequest request) {
        String validateResult = validateModifyParam(request);
        if (validateResult != null) return ApiResponseDto.fail(validateResult);
        return orderService.update(request);
    }

    private String validateModifyParam(OrderModifyRequest request) {
        if (request.id() == null || request.id().isEmpty()) return "id가 필요합니다.";
        if (request.status() == null) return "변경할 상태가 필요합니다.";
        return null;
    }

    private String validateRegisterParam(OrderRegisterRequest request) {
        if (request.sellerId() == null || request.sellerId().isEmpty()) return "판매자 id가 필요합니다.";
        if (request.buyerId() == null || request.buyerId().isEmpty()) return "구매자 id가 필요합니다.";
        if (request.auctionId() == null || request.auctionId().isEmpty()) return "경매 id가 필요합니다.";
        if (request.winningAmount() == null) return "낙찰가가 필요합니다.";
        if (request.winningDate() == null) return "낙찰이 확정된 일자가 필요합니다.";

        return null;
    }
}
