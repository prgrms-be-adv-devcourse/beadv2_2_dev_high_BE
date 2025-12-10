package com.dev_high.deposit.presentation;

import com.dev_high.deposit.application.DepositOrderService;
import com.dev_high.deposit.application.dto.DepositOrderInfo;
import com.dev_high.deposit.presentation.dto.DepositOrderCreateRequest;
import com.dev_high.deposit.presentation.dto.DepositOrderUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/deposit/orders")
@RequiredArgsConstructor
public class DepositOrderController {
    private final DepositOrderService depositOrderService;

    @Operation(summary = "예치금 주문 생성", description = "예치금 주문을 생성하고 저장")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositOrderInfo createOrder(@Valid @RequestBody DepositOrderCreateRequest request) {
        return depositOrderService.createOrder(request.toCommand());
    }

    @Operation(summary = "예치금 주문 사용자 ID별 조회", description = "사용자 ID로 결제 실패 이력을 조회")
    @GetMapping("/{userId}")
    public Page<DepositOrderInfo> findByUserId(@PathVariable String userId, Pageable pageable) {
        return depositOrderService.findByUserId(userId, pageable);
    }

    @Operation(summary = "예치금 주문 상태 변경", description = "특정 주문의 상태를 변경")
    @PatchMapping("/status")
    public DepositOrderInfo updateOrderStatus(@Valid @RequestBody DepositOrderUpdateRequest request) {
        return depositOrderService.updateOrderStatus(request.toCommand());
    }

}
