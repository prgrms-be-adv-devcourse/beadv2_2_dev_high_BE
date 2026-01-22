package com.dev_high.deposit.order.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.type.DepositOrderStatus;
import com.dev_high.deposit.order.application.DepositOrderService;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.presentation.dto.DepositOrderRequest;
import com.dev_high.deposit.order.presentation.dto.DepositOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        DepositOrderDto.CreatePaymentCommand command = request.toCommand(request.amount(), request.deposit());
        DepositOrderDto.Info info = depositOrderService.createPaymentOrder(command);
        DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "예치금 충전 주문 생성", description = "예치금 충전 주문을 생성하고 저장")
    @PostMapping("/orders/deposit-charge")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositOrderResponse.Detail> createDepositPaymentOrder(@RequestBody @Valid DepositOrderRequest.CreateDepositPayment request) {
        DepositOrderDto.CreateDepositPaymentCommand command = request.toCommand(request.amount());
        DepositOrderDto.Info info = depositOrderService.createDepositPaymentOrder(command);
        DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 주문 조회", description = "사용자 ID로 예치금 주문 내역 을 조회")
    @GetMapping("/orders/me")
    public ApiResponseDto<Page<DepositOrderResponse.Detail>> findByUserId(Pageable pageable) {
        Page<DepositOrderDto.Info> infos = depositOrderService.findByUserId(pageable);
        Page<DepositOrderResponse.Detail> response = infos.map(DepositOrderResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "주문 ID의 예치금 사용 처리", description = "주문 ID의 예치금 사용 처리")
    @PostMapping("/orders/pay-by-deposit")
    public ApiResponseDto<?> payOrderByDeposit(@RequestBody @Valid DepositOrderRequest.OrderPayWithDeposit request) {
        DepositOrderDto.OrderPayWithDepositCommand command = request.toCommand(request.id());
        DepositOrderDto.Info info = depositOrderService.payOrderByDeposit(command);
        if (info.status().equals(DepositOrderStatus.DEPOSIT_APPLIED_ERROR)) {
            return ApiResponseDto.fail("예치금 사용에 실패하였습니다", info.status().name());
        } else {
            DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
            return ApiResponseDto.success(response);
        }
    }
}
