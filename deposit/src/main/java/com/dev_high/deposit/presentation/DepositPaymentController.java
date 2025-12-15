package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositPaymentFailureHistoryService;
import com.dev_high.deposit.application.DepositPaymentService;
import com.dev_high.deposit.application.dto.DepositPaymentFailureHistoryInfo;
import com.dev_high.deposit.application.dto.DepositPaymentInfo;
import com.dev_high.deposit.presentation.dto.DepositPaymentConfirmRequest;
import com.dev_high.deposit.presentation.dto.DepositPaymentCreateRequest;
import com.dev_high.deposit.presentation.dto.DepositPaymentFailureHistoryRequest;
import com.dev_high.deposit.presentation.dto.DepositPaymentFailureHistorySearchRequest;
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
    public DepositPaymentInfo createPayment(@Valid @RequestBody DepositPaymentCreateRequest request) {
        return paymentService.createPayment(request.toCommand());
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 결제 조회", description = "예치금 결제 내역을 사용자 ID로 조회")
    @GetMapping("/payments/me")
    public Page<DepositPaymentInfo> findByUserId(Pageable pageable) {
        return paymentService.findPaymentsByUserId(pageable);
    }

    @Operation(summary = "토스 결제 승인", description = "토스 결제 완료 후 paymentKey,orderId,amount를 전달받아 결제를 승인한다.")
    @PostMapping("/payments/confirm")
    public DepositPaymentInfo confirmPayment(@RequestBody DepositPaymentConfirmRequest request) {
        return paymentService.confirmPayment(request.toCommand());
    }

    @Operation(summary = "결제 실패 이력 생성", description = "결제 실패 이벤트 발생 시 이력을 생성")
    @PostMapping("/payments/fail")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositPaymentFailureHistoryInfo createHistory(@Valid @RequestBody DepositPaymentFailureHistoryRequest request) {
        return historyService.createHistory(request.toCommand());
    }

    @Operation(summary = "결제 실패 이력 전체 조회", description = "모든 결제 실패 이력을 조회")
    @GetMapping("/payments/fail/list")
    public Page<DepositPaymentFailureHistoryInfo> findAll(Pageable pageable) {
        return historyService.findAll(pageable);
    }

    @Operation(summary = "실패 이력 ID별 조회", description = "실패 이력 ID로 결제 실패 이력을 조회")
    @GetMapping("/payments/fail/{historyId}")
    public DepositPaymentFailureHistoryInfo findHistoryById(@PathVariable Long historyId) {
        return historyService.findHistoryById(historyId);
    }

    @Operation(summary = "결제 ID별 실패 이력 조회", description = "결제 ID로 결제 실패 이력을 조회")
    @PostMapping("/payments/fail/paymentId")
    public Page<DepositPaymentFailureHistoryInfo> findHistoriesByPaymentId(
            @RequestBody DepositPaymentFailureHistorySearchRequest request, Pageable pageable) {
        return historyService.findHistoriesByPaymentId(request.toCommand(), pageable);
    }

    @Operation(summary = "사용자 ID별 실패 이력 조회", description = "사용자 ID로 결제 실패 이력을 조회")
    @PostMapping("/search/user/userId")
    public Page<DepositPaymentFailureHistoryInfo> findHistoriesByUserId(
            @RequestBody DepositPaymentFailureHistorySearchRequest request, Pageable pageable) {
        return historyService.findHistoriesByUserId(request.toCommand(), pageable);
    }

}
