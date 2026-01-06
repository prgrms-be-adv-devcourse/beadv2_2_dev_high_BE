package com.dev_high.settlement.settle.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.settle.application.SettlementDailySummary;
import com.dev_high.settlement.settle.application.SettlementService;
import com.dev_high.settlement.settle.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.settle.presentation.dto.SettlementResponse;
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
        return settlementService.findBySellerId(pageable);
    }

    @GetMapping("/summary")
    public ApiResponseDto<Page<SettlementDailySummary>> getSettlementSummary(Pageable pageable) {

        return settlementService.findSettlementSummary(pageable);
    }

    @GetMapping("{settleId}")
    public ApiResponseDto<SettlementResponse> findById(@PathVariable String settleId) {
        return settlementService.findById(settleId);
    }

    @PutMapping("{settleId}")
    public ApiResponseDto<SettlementResponse> update(@RequestBody SettlementModifyRequest request) {
        return settlementService.update(request);
    }

}
