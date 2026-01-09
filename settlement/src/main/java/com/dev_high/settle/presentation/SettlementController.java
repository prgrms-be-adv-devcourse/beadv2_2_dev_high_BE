package com.dev_high.settle.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settle.application.SettlementDailySummary;
import com.dev_high.settle.application.SettlementService;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settle")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ApiResponseDto<Page<SettlementResponse>> findAllHistory(Pageable pageable) {
        return ApiResponseDto.success(settlementService.findBySellerId(pageable));
    }

    @GetMapping("/summary")
    public ApiResponseDto<Page<SettlementDailySummary>> getSettlementSummary(Pageable pageable) {

        return ApiResponseDto.success(settlementService.findSettlementSummary(pageable));
    }

    @GetMapping("{settleId}")
    public ApiResponseDto<SettlementResponse> findById(@PathVariable String settleId) {
        return ApiResponseDto.success(settlementService.findById(settleId));
    }

}
