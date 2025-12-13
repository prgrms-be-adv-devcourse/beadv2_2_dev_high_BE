package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositOrderService;
import com.dev_high.deposit.application.dto.DepositOrderInfo;
import com.dev_high.deposit.presentation.dto.DepositOrderCreateRequest;
import com.dev_high.deposit.presentation.dto.DepositOrderUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/deposit")
@RequiredArgsConstructor
@Tag(name = "DepositOrder", description = "예치금 주문 API")
public class DepositOrderController {
    private final DepositOrderService depositOrderService;

    @Operation(summary = "예치금 주문 생성", description = "예치금 주문을 생성하고 저장")
    @PostMapping("/order")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositOrderInfo createOrder(@Valid @RequestBody DepositOrderCreateRequest request) {
        return depositOrderService.createOrder(request.toCommand());
    }

    @Operation(summary = "로그인한 사용자 ID의 예치금 주문 조회", description = "사용자 ID로 예치금 주문 내역 을 조회")
    @GetMapping("/order/me")
    public Page<DepositOrderInfo> findByUserId(Pageable pageable) {
        return depositOrderService.findByUserId(pageable);
    }

    @Operation(summary = "예치금 주문 상태 변경", description = "특정 주문의 상태를 변경")
    @PatchMapping("/order/status")
    public DepositOrderInfo updateOrderStatus(@Valid @RequestBody DepositOrderUpdateRequest request) {
        return depositOrderService.updateOrderStatus(request.toCommand());
    }

}
