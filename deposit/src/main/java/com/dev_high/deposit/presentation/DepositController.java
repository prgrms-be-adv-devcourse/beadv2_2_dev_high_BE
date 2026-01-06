package com.dev_high.deposit.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.application.DepositService;
import com.dev_high.deposit.application.dto.DepositDto;
import com.dev_high.deposit.presentation.dto.DepositRequest;
import com.dev_high.deposit.presentation.dto.DepositResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
@Tag(name = "Deposit", description = "예치금 계좌 정보 API")
public class DepositController {
    private final DepositService depositService;

    @Operation(summary = "예치금 계좌 생성", description = "예치금 계좌를 생성하고 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositResponse.Detail> createDepositAccount(@RequestBody @Valid DepositRequest.Create request) {
        DepositDto.CreateCommand command = request.toCommand(request.userId());
        DepositDto.Info info = depositService.createDepositAccount(command);
        DepositResponse.Detail response = DepositResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 계좌 조회", description = "예치금 계좌ID로 예치금 계좌를 조회")
    @GetMapping("/me")
    public ApiResponseDto<DepositResponse.Detail> findDepositAccountById() {
        DepositDto.Info info = depositService.findDepositAccountById();
        DepositResponse.Detail response = DepositResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "예치금 사용", description = "예치금 사용")
    @PostMapping("/usages")
    public ApiResponseDto<DepositResponse.Detail> usageDeposit(@RequestBody @Valid DepositRequest.Usage request) {
        DepositDto.UsageCommand command = request.toCommand(request.userId(), request.depositOrderId(), request.type(), request.amount());
        DepositDto.Info info = depositService.updateBalance(command);
        DepositResponse.Detail response = DepositResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }
}
