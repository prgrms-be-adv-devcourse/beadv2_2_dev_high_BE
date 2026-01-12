package com.dev_high.user.deposit.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.deposit.application.*;
import com.dev_high.user.deposit.application.dto.*;
import com.dev_high.user.deposit.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/deposit")
@RequiredArgsConstructor
public class AdminController {

    /**
     * 예치금 관련 임시 어드민 컨트롤러
     */

    private final DepositService depositService;
    private final DepositHistoryService historyService;


    @Operation(summary = "예치금 계좌 생성", description = "예치금 계좌를 생성하고 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositResponse.Detail> createDepositAccount(@RequestBody @Valid DepositRequest.Create request) {
        DepositDto.CreateCommand command = request.toCommand(request.userId());
        DepositDto.Info info = depositService.createDepositAccount(command);
        DepositResponse.Detail response = DepositResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "예치금 이력 생성", description = "예치금 이력을 생성하고 저장")
    @PostMapping("/histories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositHistoryResponse.Detail> createHistory(@RequestBody @Valid DepositHistoryRequest.Create request) {
        DepositHistoryDto.CreateCommand command = request.toCommand(request.userId(), request.orderId(), request.type(), request.amount(), request.nowBalance());
        DepositHistoryDto.Info info = historyService.createHistory(command);
        DepositHistoryResponse.Detail response = DepositHistoryResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }
}
