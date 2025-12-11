package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositPaymentFailureHistoryService;
import com.dev_high.deposit.application.dto.DepositPaymentFailureHistoryInfo;
import com.dev_high.deposit.presentation.dto.DepositPaymentFailureHistoryRequest;
import com.dev_high.deposit.presentation.dto.DepositPaymentFailureHistorySearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/deposit/failures")
@RequiredArgsConstructor
public class DepositPaymentFailureHistoryController {
    private final DepositPaymentFailureHistoryService historyService;

    @Operation(summary = "결제 실패 이력 생성", description = "결제 실패 이벤트 발생 시 이력을 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositPaymentFailureHistoryInfo createHistory(@Valid @RequestBody DepositPaymentFailureHistoryRequest request) {
        return historyService.createHistory(request.toCommand());
    }

    @Operation(summary = "결제 실패 이력 전체 조회", description = "모든 결제 실패 이력을 조회")
    @GetMapping
    public Page<DepositPaymentFailureHistoryInfo> findAll(Pageable pageable) {
        return historyService.findAll(pageable);
    }

    @Operation(summary = "실패 이력 ID별 조회", description = "실패 이력 ID로 결제 실패 이력을 조회")
    @GetMapping("/{historyId}")
    public DepositPaymentFailureHistoryInfo findHistoryById(@PathVariable Long historyId) {
        return historyService.findHistoryById(historyId);
    }

    @Operation(summary = "결제 ID별 실패 이력 조회", description = "결제 ID로 결제 실패 이력을 조회")
    @PostMapping("/search/payment")
    public Page<DepositPaymentFailureHistoryInfo> findHistoriesByPaymentId(
            @RequestBody DepositPaymentFailureHistorySearchRequest request, Pageable pageable) {
        return historyService.findHistoriesByPaymentId(request.toCommand(), pageable);
    }

    @Operation(summary = "사용자 ID별 실패 이력 조회", description = "사용자 ID로 결제 실패 이력을 조회")
    @PostMapping("/search/user")
    public Page<DepositPaymentFailureHistoryInfo> findHistoriesByUserId(
            @RequestBody DepositPaymentFailureHistorySearchRequest request, Pageable pageable) {
        return historyService.findHistoriesByUserId(request.toCommand(), pageable);
    }

}
