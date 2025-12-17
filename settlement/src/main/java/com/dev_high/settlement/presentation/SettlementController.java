package com.dev_high.settlement.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.application.SettlementService;
import com.dev_high.settlement.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.presentation.dto.SettlementResponse;
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

    @GetMapping("/{settleId}")
    public ApiResponseDto<SettlementResponse> findById(@PathVariable String settleId) {
        return settlementService.findById(settleId);
    }

    @PutMapping("/update")
    public ApiResponseDto<SettlementResponse> update(@RequestBody SettlementModifyRequest request) {
        return settlementService.update(request);
    }

}
