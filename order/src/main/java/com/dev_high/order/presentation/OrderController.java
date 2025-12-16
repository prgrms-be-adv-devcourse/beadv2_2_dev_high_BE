package com.dev_high.order.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.application.OrderService;
import com.dev_high.order.application.dto.SettlementRegisterRequest;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public ApiResponseDto<List<OrderResponse>> getAllOrders() {
    return orderService.getAllOrders();
  }

  @GetMapping("/sold")
  public ApiResponseDto<List<OrderResponse>> soldList() {
    return orderService.soldList();
  }

  @GetMapping("/bought")
  public ApiResponseDto<List<OrderResponse>> boughtList() {
    return orderService.boughtList();
  }


  @GetMapping("{orderId}")
  public ApiResponseDto<OrderResponse> detail(@PathVariable(name = "orderId") String orderId) {
    return orderService.findOne(orderId);
  }


  @PostMapping
  public ApiResponseDto<OrderResponse> create(@RequestBody OrderRegisterRequest request) {

    return orderService.create(request);
  }

  @PutMapping
  public ApiResponseDto<OrderResponse> update(@RequestBody OrderModifyRequest request) {
    return orderService.update(request);
  }


  @GetMapping("confirmed")
  public ResponseEntity<List<SettlementRegisterRequest>> fetchConfirmedOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size) {

    List<SettlementRegisterRequest> confirmedOrders = orderService.findConfirmedOrders(page, size);

    return ResponseEntity.ok(confirmedOrders);
  }

}
