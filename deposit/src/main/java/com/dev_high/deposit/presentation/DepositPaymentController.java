package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositPaymentService;
import com.dev_high.deposit.application.dto.DepositPaymentInfo;
import com.dev_high.deposit.presentation.dto.DepositPaymentConfirmRequest;
import com.dev_high.deposit.presentation.dto.DepositPaymentCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/deposit/payments")
@RequiredArgsConstructor
public class DepositPaymentController {
    private final DepositPaymentService paymentService;

    @Operation(summary = "예치금 결제 생성", description = "결제 승인 요청 전, READY 상태의 결제를 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositPaymentInfo createPayment(@Valid @RequestBody DepositPaymentCreateRequest request) {
        return paymentService.createPayment(request.toCommand());
    }

    @Operation(summary = "사용자 ID별 예치금 결제 내역 조회", description = "예치금 결제 내역을 사용자 ID로 조회")
    @GetMapping("{userId}")
    public Page<DepositPaymentInfo> findByUserId(@PathVariable String userId, Pageable pageable) {
        return paymentService.findPaymentsByUserId(userId, pageable);
    }

    @Operation(summary = "토스 결제 승인", description = "토스 결제 완료 후 paymentKey,orderId,amount를 전달받아 결제를 승인한다.")
    @PostMapping("/confirm")
    public DepositPaymentInfo confirmPayment(@RequestBody DepositPaymentConfirmRequest request) {
        return paymentService.confirmPayment(request.toCommand());
    }
}
