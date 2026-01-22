package com.dev_high.deposit.order.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.order.application.DepositOrderService;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.presentation.dto.DepositOrderRequest;
import com.dev_high.deposit.order.presentation.dto.DepositOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "DepositOrder", description = "예치금 주문 API")
public class DepositOrderController {
    private final DepositOrderService depositOrderService;

    @Operation(summary = "결제 주문 생성", description = "결제 주문을 생성하고 저장")
    @PostMapping("/orders/order-payment")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositOrderResponse.Detail> createPaymentOrder(@RequestBody @Valid DepositOrderRequest.CreatePayment request) {
        log.info("[PaymentOrder API] PATH : /orders/order-payment request received. amount={}, deposit={}", request.amount(), request.deposit());
        DepositOrderDto.CreatePaymentCommand command = request.toCommand(request.amount(), request.deposit());
        DepositOrderDto.Info info = depositOrderService.createPaymentOrder(command);
        DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
        log.info("[PaymentOrder API] PATH : /orders/order-payment success. orderId={}, amount={}, deposit={}, paidAmount={}, status={}", response.id(), response.amount(), response.deposit(), response.paidAmount(), response.status());
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "예치금 충전 주문 생성", description = "예치금 충전 주문을 생성하고 저장")
    @PostMapping("/orders/deposit-charge")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositOrderResponse.Detail> createDepositPaymentOrder(@RequestBody @Valid DepositOrderRequest.CreateDepositPayment request) {
        log.info("[PaymentOrder API] PATH : /orders/deposit-charge request received. amount={}", request.amount());
        DepositOrderDto.CreateDepositPaymentCommand command = request.toCommand(request.amount());
        DepositOrderDto.Info info = depositOrderService.createDepositPaymentOrder(command);
        DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
        log.info("[PaymentOrder API] PATH : /orders/deposit-charge success. orderId={}, amount={}, paidAmount={}, status={}", response.id(), response.amount(), response.paidAmount(), response.status());
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 주문 조회", description = "사용자 ID로 예치금 주문 내역 을 조회")
    @GetMapping("/orders/me")
    public ApiResponseDto<Page<DepositOrderResponse.Detail>> findByUserId(Pageable pageable) {
        log.info("[PaymentOrder API] PATH : /orders/me request received. page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<DepositOrderDto.Info> infos = depositOrderService.findByUserId(pageable);
        Page<DepositOrderResponse.Detail> response = infos.map(DepositOrderResponse.Detail::from);
        log.info("[PaymentOrder API] PATH : /orders/me success. totalElements={}, totalPages={}", response.getTotalElements(), response.getTotalPages());
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "주문 ID의 예치금 사용 처리", description = "주문 ID의 예치금 사용 처리")
    @PostMapping("/orders/pay-by-deposit")
    public ApiResponseDto<?> payOrderByDeposit(@RequestBody @Valid DepositOrderRequest.OrderPayWithDeposit request) {
        DepositOrderDto.OrderPayWithDepositCommand command = request.toCommand(request.id());
        try {
            DepositOrderDto.Info info = depositOrderService.payOrderByDeposit(command);
            return ApiResponseDto.success(DepositOrderResponse.Detail.from(info));
        } catch (Exception e) {
            return ApiResponseDto.fail("예치금 사용에 실패하였습니다", e.getMessage());
        }
    }
}
