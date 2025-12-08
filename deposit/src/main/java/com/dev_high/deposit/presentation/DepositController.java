package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositService;
import com.dev_high.deposit.application.dto.DepositInfo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/deposit")
@RequiredArgsConstructor
public class DepositController {
    private final DepositService depositService;

    @Operation(summary = "예치금 계좌 생성", description = "예치금 계좌를 생성하고 저장")
    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositInfo createDepositAccount(
            @PathVariable @Validated String userId) {
        return depositService.createDepositAccount(userId);
    }
}
