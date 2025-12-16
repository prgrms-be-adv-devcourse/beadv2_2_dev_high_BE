package com.dev_high.settlement.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.application.SettlementService;
import com.dev_high.settlement.presentation.dto.SettlementModifyRequest;
import com.dev_high.settlement.presentation.dto.SettlementResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settle")
public class SettlementController {

  private final SettlementService settlementService;

  @GetMapping("/history")
  public ApiResponseDto<List<SettlementResponse>> findAllHistory() {
    return settlementService.findBySellerId();
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
