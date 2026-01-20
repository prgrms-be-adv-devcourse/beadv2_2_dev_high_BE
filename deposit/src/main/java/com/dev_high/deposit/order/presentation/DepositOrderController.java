package com.dev_high.deposit.order.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.deposit.order.application.DepositOrderService;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.dev_high.deposit.order.presentation.dto.DepositOrderRequest;
import com.dev_high.deposit.order.presentation.dto.DepositOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "DepositOrder", description = "예치금 주문 API")
public class DepositOrderController {
    private final DepositOrderService depositOrderService;

    @Operation(summary = "주문 생성", description = "주문을 생성하고 저장")
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<DepositOrderResponse.Detail> createOrder(@RequestBody @Valid DepositOrderRequest.Create request) {
        DepositOrderDto.CreateCommand command = request.toCommand(request.amount(), request.deposit());
        DepositOrderDto.Info info = depositOrderService.createOrder(command);
        DepositOrderResponse.Detail response = DepositOrderResponse.Detail.from(info);
        return ApiResponseDto.success(response);
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 주문 조회", description = "사용자 ID로 예치금 주문 내역 을 조회")
    @GetMapping("/orders/me")
    public ApiResponseDto<Page<DepositOrderResponse.Detail>> findByUserId(Pageable pageable) {
        Page<DepositOrderDto.Info> infos = depositOrderService.findByUserId(pageable);
        Page<DepositOrderResponse.Detail> response = infos.map(DepositOrderResponse.Detail::from);
        return ApiResponseDto.success(response);
    }



}
