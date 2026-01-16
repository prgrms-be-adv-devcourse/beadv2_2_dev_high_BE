package com.dev_high.deposit.payment.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.order.application.DepositOrderService;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.presentation.dto.DepositOrderRequest;
import com.dev_high.deposit.order.presentation.dto.DepositOrderResponse;
import com.dev_high.deposit.payment.application.DepositPaymentFailureHistoryService;
import com.dev_high.deposit.payment.application.DepositPaymentService;
import com.dev_high.deposit.payment.application.dto.DepositPaymentDto;
import com.dev_high.deposit.payment.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.payment.presentation.dto.DepositPaymentFailureHistoryResponse;
import com.dev_high.deposit.payment.presentation.dto.DepositPaymentRequest;
import com.dev_high.deposit.payment.presentation.dto.DepositPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class PaymentsAdminController {
    private final DepositOrderService depositOrderService;
    private final DepositPaymentService paymentService;
    private final DepositPaymentFailureHistoryService failureHistoryService;

    @Operation(summary = "예치금 주문 상태 변경", description = "특정 주문의 상태를 변경")
    @PatchMapping("/payments/orders/status")
    public ApiResponseDto<DepositOrderResponse.Detail> updateOrderStatus(@RequestBody @Valid DepositOrderRequest.Update request) {
        DepositOrderDto.ChangeOrderStatusCommand command = request.toCommand(request.id(), request.status());
        DepositOrderDto.Info info = depositOrderService.ChangeOrderStatus(command);
        DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "예치금 결제 생성", description = "결제 승인 요청 전, READY 상태의 결제를 생성")
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositPaymentResponse.Detail> createPayment(@RequestBody @Valid DepositPaymentRequest.Create request) {
        DepositPaymentDto.CreateCommand command = request.toCommand(request.orderId(), request.userId(), request.amount());
        DepositPaymentDto.Info info =  paymentService.createPayment(command);
        DepositPaymentResponse.Detail response = DepositPaymentResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "결제 실패 이력 전체 조회", description = "모든 결제 실패 이력을 조회")
    @GetMapping("/payments/fail/list")
    public ApiResponseDto<Page<DepositPaymentFailureHistoryResponse.Detail>> findAll(Pageable pageable) {
        Page<DepositPaymentFailureDto.Info> infos = failureHistoryService.findAll(pageable);
        Page<DepositPaymentFailureHistoryResponse.Detail> response = infos.map(DepositPaymentFailureHistoryResponse.Detail::from);
        return ApiResponseDto.success(response);
    }
}
