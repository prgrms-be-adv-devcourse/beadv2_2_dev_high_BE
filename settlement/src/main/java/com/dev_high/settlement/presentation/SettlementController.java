package com.dev_high.settlement.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.application.SettlementService;
import com.dev_high.settlement.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/history")
    public ApiResponseDto<List<SettlementResponse>> findAllHistory(@RequestParam String  sellerId) {
        return settlementService.findBySellerId(sellerId);
    }

    @GetMapping("/detail")
    public ApiResponseDto<SettlementResponse> findById(@RequestParam String  settlementId) {
        return settlementService.findById(settlementId);
    }

    @PutMapping("/update")
    public ApiResponseDto<SettlementResponse> update(@RequestBody SettlementModifyRequest request) {
        return settlementService.update(request);
    }

}
