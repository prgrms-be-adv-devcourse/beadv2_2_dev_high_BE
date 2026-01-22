package com.dev_high.deposit.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.application.DepositHistoryService;
import com.dev_high.deposit.application.dto.DepositHistoryDto;
import com.dev_high.deposit.domain.DepositType;
import com.dev_high.deposit.presentation.dto.DepositHistoryRequest;
import com.dev_high.deposit.presentation.dto.DepositHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
@Tag(name = "DepositHistory", description = "예치금 이력 API")
public class DepositHistoryController {
    private final DepositHistoryService historyService;


    @Operation(summary = "로그인한 사용자 ID의 예치금 이력 조회", description = "예치금 이력 내역을 사용자 ID로 조회")
    @GetMapping("/histories/me")
    public ApiResponseDto<Page<DepositHistoryResponse.Detail>> findByUserId(@RequestParam Optional<DepositType> type, Pageable pageable) {
        Page<DepositHistoryDto.Info> infos = historyService.findHistoriesByUserId(type.orElse(null),pageable);
        Page<DepositHistoryResponse.Detail> response = infos.map(DepositHistoryResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

}
