package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositHistoryService;
import com.dev_high.deposit.application.dto.DepositHistoryInfo;
import com.dev_high.deposit.presentation.dto.DepositHistoryCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/deposit/histories")
@RequiredArgsConstructor
public class DepositHistoryController {
    private final DepositHistoryService historyService;

    @Operation(summary = "예치금 계좌 생성", description = "예치금 계좌를 생성하고 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositHistoryInfo createHistory(@Valid @RequestBody DepositHistoryCreateRequest request) {
        return historyService.createHistory(request.toCommand());
    }

    @Operation(summary = "사용자 ID별 예치금 계좌 조회", description = "예치금 계좌를 사용자 ID로 조회")
    @GetMapping("{userId}")
    public Page<DepositHistoryInfo> findByUserId(@PathVariable String userId, Pageable pageable) {
        return historyService.findHistoriesByUserId(userId, pageable);
    }

}
