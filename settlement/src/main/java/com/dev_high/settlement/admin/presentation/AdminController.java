package com.dev_high.settlement.admin.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.settlement.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.settlement.settle.application.dto.SettlementRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {


    @GetMapping("orders")
    public ApiResponseDto<Page<?>> getAllOrders(@RequestBody OrderRegisterRequest request) {

        return ApiResponseDto.fail(null);
    }

    @PostMapping("orders")
    public ApiResponseDto<?> createOrder(@RequestBody OrderRegisterRequest request) {

        return ApiResponseDto.fail(null);
    }

    @PutMapping("orders")
    public ApiResponseDto<?> updateOrder(@RequestBody OrderRegisterRequest request) {

        return ApiResponseDto.fail(null);
    }

    @DeleteMapping("orders")
    public ApiResponseDto<?> deleteOrder() {

        return ApiResponseDto.fail(null);
    }



    @GetMapping("settles")
    public ApiResponseDto<Page<?>> getAllSelttles() {

        return ApiResponseDto.fail(null);
    }

    @PostMapping("settles")
    public ApiResponseDto<?> createSettle(@RequestBody SettlementRegisterRequest request) {

        return ApiResponseDto.fail(null);
    }

    @PutMapping("settles")
    public ApiResponseDto<?> updateSettle() {

        return ApiResponseDto.fail(null);
    }

    @DeleteMapping("settles")
    public ApiResponseDto<?> deleteSettle() {

        return ApiResponseDto.fail(null);
    }


}
