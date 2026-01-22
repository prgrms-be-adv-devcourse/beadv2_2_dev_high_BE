package com.dev_high.deposit.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.application.DepositHistoryService;
import com.dev_high.deposit.application.dto.DepositHistoryDto;
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

@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
@Tag(name = "DepositHistory", description = "예치금 이력 API")
public class DepositHistoryController {
    private final DepositHistoryService historyService;

    @Operation(summary = "예치금 이력 생성", description = "예치금 이력을 생성하고 저장")
    @PostMapping("/histories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositHistoryResponse.Detail> createHistory(@RequestBody @Valid DepositHistoryRequest.Create request) {
        DepositHistoryDto.CreateCommand command = request.toCommand(request.userId(), request.orderId(), request.type(), request.amount(), request.nowBalance());
        DepositHistoryDto.Info info = historyService.createHistory(command);
        DepositHistoryResponse.Detail response = DepositHistoryResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 이력 조회", description = "예치금 이력 내역을 사용자 ID로 조회")
    @GetMapping("/histories/me")
    public ApiResponseDto<Page<DepositHistoryResponse.Detail>> findByUserId(Pageable pageable) {
        Page<DepositHistoryDto.Info> infos = historyService.findHistoriesByUserId(pageable);
        Page<DepositHistoryResponse.Detail> response = infos.map(DepositHistoryResponse.Detail::from);
        return ApiResponseDto.success(response);
    }

}
