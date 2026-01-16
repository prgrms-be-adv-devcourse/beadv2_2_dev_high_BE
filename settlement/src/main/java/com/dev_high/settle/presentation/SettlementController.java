package com.dev_high.settle.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settle.application.SettlementService;
import com.dev_high.settle.presentation.dto.SettlementGroupResponse;
import com.dev_high.settle.presentation.dto.SettlementResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settle")
@Tag(name = "Settlement", description = "정산 관리 API")

public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ApiResponseDto<Page<SettlementResponse>> findAllHistory(Pageable pageable) {
        return ApiResponseDto.success(settlementService.findBySellerId(pageable));
    }

    @GetMapping("/summary")
    public ApiResponseDto<Page<SettlementGroupResponse>> getSettlementSummary(Pageable pageable) {

        return ApiResponseDto.success(settlementService.findSettlementSummary(pageable));
    }

    @GetMapping("/group/{groupId}/items")
    public ApiResponseDto<Page<SettlementResponse>> findByGroupId(@PathVariable String groupId,
                                                                  Pageable pageable) {
        return ApiResponseDto.success(settlementService.findByGroupId(groupId, pageable));
    }

    @GetMapping("{settleId}")
    public ApiResponseDto<SettlementResponse> findById(@PathVariable String settleId) {
        return ApiResponseDto.success(settlementService.findById(settleId));
    }

}
