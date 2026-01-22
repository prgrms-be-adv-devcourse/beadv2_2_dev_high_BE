package com.dev_high.user.deposit.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.deposit.application.DepositService;
import com.dev_high.user.deposit.application.dto.DepositDto;
import com.dev_high.user.deposit.presentation.dto.DepositRequest;
import com.dev_high.user.deposit.presentation.dto.DepositResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
@Tag(name = "Deposit", description = "예치금 계좌 정보 API")
public class DepositController {
    private final DepositService depositService;



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
        try {
            DepositDto.Info info = depositService.updateBalance(command);
            log.info("[Deposit API] PATH : /usages success userId={} depositOrderId={} type={} amount={}",
                    request.userId(), request.depositOrderId(), request.type(), request.amount());
            return ApiResponseDto.success(DepositResponse.Detail.from(info));
        } catch (NoSuchElementException e) {
            log.warn("[Deposit API] PATH : /usages failed - Not found userId={}",
                    request.userId());
            return ApiResponseDto.fail("예치금 계좌를 찾을 수 없습니다", e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("[Deposit API] PATH : /usages failed - Invalid request request userId={}, depositOrderId={}, type={} amount={}",
                    request.userId(), request.depositOrderId(), request.type(), request.amount(), e);
            return ApiResponseDto.fail("예치금 사용이 불가능합니다", e.getMessage());

        } catch (Exception e) {
            log.error("[Deposit API] PATH : /usages error userId={}, depositOrderId={}, type={} amount={}",
                    request.userId(), request.depositOrderId(), request.type(), request.amount(), e);
            return ApiResponseDto.error("예치금 사용 중 오류가 발생했습니다");
        }
    }
}
