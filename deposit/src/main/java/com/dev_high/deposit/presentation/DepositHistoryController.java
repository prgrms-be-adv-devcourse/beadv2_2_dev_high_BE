package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositHistoryService;
import com.dev_high.deposit.application.dto.DepositHistoryInfo;
import com.dev_high.deposit.presentation.dto.DepositHistoryCreateRequest;
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
@Tag(name = "DepositHistory", description = "예치금 이력 API")
public class DepositHistoryController {
    private final DepositHistoryService historyService;

    @Operation(summary = "예치금 이력 생성", description = "예치금 이력을 생성하고 저장")
    @PostMapping("/histories")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositHistoryInfo createHistory(@Valid @RequestBody DepositHistoryCreateRequest request) {
        return historyService.createHistory(request.toCommand());
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 이력 조회", description = "예치금 이력 내역을 사용자 ID로 조회")
    @GetMapping("/histories/me")
    public Page<DepositHistoryInfo> findByUserId(Pageable pageable) {
        return historyService.findHistoriesByUserId(pageable);
    }

}
