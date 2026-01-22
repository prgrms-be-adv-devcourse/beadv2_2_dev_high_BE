package com.dev_high.deposit.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.application.DepositPaymentFailureHistoryService;
import com.dev_high.deposit.application.DepositPaymentService;
import com.dev_high.deposit.application.dto.DepositPaymentDto;
import com.dev_high.deposit.application.dto.DepositPaymentFailureDto;
import com.dev_high.deposit.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
@Tag(name = "DepositPayment", description = "예치금 결제 API")
public class DepositPaymentController {
    private final DepositPaymentService paymentService;
    private final DepositPaymentFailureHistoryService historyService;

    @Operation(summary = "예치금 결제 생성", description = "결제 승인 요청 전, READY 상태의 결제를 생성")
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositPaymentResponse.Detail> createPayment(@RequestBody @Valid DepositPaymentRequest.Create request) {
        DepositPaymentDto.CreateCommand command = request.toCommand(request.orderId(), request.method(), request.amount());
        DepositPaymentDto.Info info =  paymentService.createPayment(command);
        DepositPaymentResponse.Detail response = DepositPaymentResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 결제 조회", description = "예치금 결제 내역을 사용자 ID로 조회")
    @GetMapping("/payments/me")
    public ApiResponseDto<Page<DepositPaymentResponse.Detail>> findByUserId(Pageable pageable) {
        Page<DepositPaymentDto.Info> infos =  paymentService.findPaymentsByUserId(pageable);
        Page<DepositPaymentResponse.Detail> response = infos.map(DepositPaymentResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "토스 결제 승인", description = "토스 결제 완료 후 paymentKey,orderId,amount를 전달받아 결제를 승인한다.")
    @PostMapping("/payments/confirm")
    public ApiResponseDto<DepositPaymentResponse.Detail> confirmPayment(@RequestBody DepositPaymentRequest.Confirm request) {
        DepositPaymentDto.ConfirmCommand command = request.toCommand(request.paymentKey(), request.orderId(), request.amount());
        DepositPaymentDto.Info info =  paymentService.confirmPayment(command);
        DepositPaymentResponse.Detail response = DepositPaymentResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "결제 실패 이력 생성", description = "결제 실패 이벤트 발생 시 이력을 생성")
    @PostMapping("/payments/fail")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositPaymentFailureHistoryResponse.Detail> createHistory(@RequestBody @Valid DepositPaymentFailureHistoryRequest.Create request) {
        DepositPaymentFailureDto.CreateCommand command = request.toCommand(request.orderId(), request.userId(), request.code(), request.message());
        DepositPaymentFailureDto.Info info = historyService.createHistory(command);
        DepositPaymentFailureHistoryResponse.Detail response = DepositPaymentFailureHistoryResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "결제 실패 이력 전체 조회", description = "모든 결제 실패 이력을 조회")
    @GetMapping("/payments/fail/list")
    public ApiResponseDto<Page<DepositPaymentFailureHistoryResponse.Detail>> findAll(Pageable pageable) {
        Page<DepositPaymentFailureDto.Info> infos = historyService.findAll(pageable);
        Page<DepositPaymentFailureHistoryResponse.Detail> response = infos.map(DepositPaymentFailureHistoryResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "실패 이력 ID별 조회", description = "실패 이력 ID로 결제 실패 이력을 조회")
    @GetMapping("/payments/fail/{historyId}")
    public ApiResponseDto<DepositPaymentFailureHistoryResponse.Detail> findHistoryById(@PathVariable Long historyId) {
        DepositPaymentFailureDto.Info info = historyService.findHistoryById(historyId);
        DepositPaymentFailureHistoryResponse.Detail response = DepositPaymentFailureHistoryResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "주문 ID별 실패 이력 조회", description = "주문 ID로 결제 실패 이력을 조회")
    @PostMapping("/payments/fail/paymentId")
    public ApiResponseDto<Page<DepositPaymentFailureHistoryResponse.Detail>> findHistoriesByOrderId(@RequestBody DepositPaymentFailureHistoryRequest.Search request, Pageable pageable) {
        DepositPaymentFailureDto.SearchCommand command = request.toCommand(request.orderId(), request.userId());
        Page<DepositPaymentFailureDto.Info> infos = historyService.findHistoriesByOrderId(command, pageable);
        Page<DepositPaymentFailureHistoryResponse.Detail> response = infos.map(DepositPaymentFailureHistoryResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "사용자 ID별 실패 이력 조회", description = "사용자 ID로 결제 실패 이력을 조회")
    @PostMapping("/search/user/userId")
    public ApiResponseDto<Page<DepositPaymentFailureHistoryResponse.Detail>> findHistoriesByUserId(@RequestBody DepositPaymentFailureHistoryRequest.Search request, Pageable pageable) {
        DepositPaymentFailureDto.SearchCommand command = request.toCommand(request.orderId(), request.userId());
        Page<DepositPaymentFailureDto.Info> infos = historyService.findHistoriesByUserId(command, pageable);
        Page<DepositPaymentFailureHistoryResponse.Detail> response = infos.map(DepositPaymentFailureHistoryResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

}
