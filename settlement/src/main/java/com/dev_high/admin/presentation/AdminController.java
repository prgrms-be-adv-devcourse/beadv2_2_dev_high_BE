package com.dev_high.admin.presentation;

import com.dev_high.admin.application.AdminService;
import com.dev_high.admin.application.dto.DashboardSellerRankItem;
import com.dev_high.admin.application.dto.DashboardTrendPoint;
import com.dev_high.admin.presentation.dto.OrderAdminSearchFilter;
import com.dev_high.admin.presentation.dto.SettlementAdminSearchFilter;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.application.OrderService;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import com.dev_high.settle.presentation.dto.SettlementGroupResponse;
import com.dev_high.settle.presentation.dto.SettlementModifyRequest;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
@Tag(name = "Admin", description = "주문/정산 관리 API")

public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    

    @GetMapping("orders")
    public ApiResponseDto<Page<OrderResponse>> getAllOrders(
        @ModelAttribute OrderAdminSearchFilter filter,
        Pageable pageable) {

        return ApiResponseDto.success(adminService.findAllOrders(filter, pageable));
    }

    @GetMapping("orders/count")
    public ApiResponseDto<Long> getOrderCount(@RequestParam(required = false) OrderStatus status) {

     return  ApiResponseDto.success(adminService.getOrderCount(status));
    }

    @GetMapping("orders/dashboard/gmv-trend")
    public ApiResponseDto<List<DashboardTrendPoint>> getGmvTrend(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @RequestParam(required = false) String groupBy,
        @RequestParam(required = false) String timezone
    ) {
        return ApiResponseDto.success(adminService.getGmvTrend(from, to, groupBy, timezone));
    }

    @GetMapping("orders/dashboard/rank/sellers")
    public ApiResponseDto<List<DashboardSellerRankItem>> getSellerRank(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String timezone
    ) {
        return ApiResponseDto.success(adminService.getSellerRank(from, to, limit, timezone));
    }

    @PostMapping("orders")
    public ApiResponseDto<?> createOrder(@RequestBody OrderRegisterRequest request) {

        return ApiResponseDto.success(orderService.create(request));
    }

    @PatchMapping("orders/pay-limit")
    public ApiResponseDto<?> updatePaymentLimitDate(@RequestBody OrderModifyRequest request) {

        return ApiResponseDto.success(adminService.updatePaymentLimitDate(request));
    }
    @PatchMapping("orders")
    public ApiResponseDto<?> updateWinOrderStatus(@RequestBody OrderModifyRequest request) {

        return ApiResponseDto.success(orderService.update(request));
    }

    @DeleteMapping("orders/{orderId}")
    public ApiResponseDto<?> deleteOrder(@PathVariable  String orderId) {

        return ApiResponseDto.success(adminService.removeWinOredr(orderId));
    }



    @GetMapping("settles")
    public ApiResponseDto<Page<SettlementGroupResponse>> getAllSelttles(
        @ModelAttribute SettlementAdminSearchFilter filter,
        Pageable pageable) {

        return ApiResponseDto.success(adminService.findAllSettlements(filter, pageable));
    }

    @GetMapping("settles/group/{groupId}/items")
    public ApiResponseDto<Page<SettlementResponse>> findSettleItemsByGroupId(
        @PathVariable String groupId,
        Pageable pageable) {
        return ApiResponseDto.success(adminService.findSettlementsByGroupId(groupId, pageable));
    }

    @PostMapping("settles/{orderId}")
    public ApiResponseDto<?> createSettle(@PathVariable String orderId) {
        return ApiResponseDto.success(adminService.createSettle(orderId));
    }

    @PostMapping("settles/run")
    public ApiResponseDto<?> runSettlementBatch(@RequestParam(defaultValue = "WAITING") String status) {
        return ApiResponseDto.success(adminService.runSettlementBatch(status));
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
