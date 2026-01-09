package com.dev_high.settlement.admin.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.admin.application.AdminService;
import com.dev_high.settlement.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.settlement.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.settlement.order.application.OrderService;
import com.dev_high.settlement.order.presentation.dto.OrderModifyRequest;
import com.dev_high.settlement.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.settlement.settle.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.order.presentation.dto.OrderResponse;
import com.dev_high.settlement.settle.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    

    @GetMapping("orders")
    public ApiResponseDto<Page<OrderResponse>> getAllOrders(
        @ModelAttribute OrderAdminSearchFilter filter,
        Pageable pageable) {

        return ApiResponseDto.success(adminService.findAllOrders(filter, pageable));
    }

    @PostMapping("orders")
    public ApiResponseDto<?> createOrder(@RequestBody OrderRegisterRequest request) {

        return ApiResponseDto.success(orderService.create(request));
    }

    @PatchMapping("orders")
    public ApiResponseDto<?> updateOrder(@RequestBody OrderModifyRequest request) {

        return ApiResponseDto.success(orderService.update(request));
    }

    @DeleteMapping("orders")
    public ApiResponseDto<?> deleteOrder() {

        return ApiResponseDto.fail(null);
    }



    @GetMapping("settles")
    public ApiResponseDto<Page<SettlementResponse>> getAllSelttles(
        @ModelAttribute SettlementAdminSearchFilter filter,
        Pageable pageable) {

        return ApiResponseDto.success(adminService.findAllSettlements(filter, pageable));
    }

    @PostMapping("settles/{orderId}")
    public ApiResponseDto<?> createSettle(@PathVariable  String orderId) {
        return ApiResponseDto.success(adminService.createSettle(orderId));
    }


    @PatchMapping("settles")
    public ApiResponseDto<?> updateSettle(SettlementModifyRequest request) {

        return ApiResponseDto.success(adminService.updateSettle(request));
    }

    @DeleteMapping("settles")
    public ApiResponseDto<?> deleteSettle() {

        return ApiResponseDto.fail(null);
    }


}
