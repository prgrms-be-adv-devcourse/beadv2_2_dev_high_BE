package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositService;
import com.dev_high.deposit.application.dto.DepositInfo;
import com.dev_high.deposit.presentation.dto.DepositCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
    public DepositInfo createDepositAccount(
            @RequestBody @Validated DepositCreateRequest request) {
        return depositService.createDepositAccount(request.toCommand());
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 계좌 조회", description = "예치금 계좌ID로 예치금 계좌를 조회")
    @GetMapping("/me")
    public DepositInfo findDepositAccountById() {
        return depositService.findDepositAccountById();
    }
}
