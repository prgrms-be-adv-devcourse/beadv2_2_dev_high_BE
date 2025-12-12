package com.dev_high.order.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.exception.CustomException;
import com.dev_high.order.application.dto.SettlementRegisterRequest;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.order.presentation.dto.OrderModifyRequest;
import com.dev_high.order.presentation.dto.OrderRegisterRequest;
import com.dev_high.order.presentation.dto.OrderResponse;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;

  public ApiResponseDto<List<OrderResponse>> getAllOrders() {
    List<Order> found = orderRepository.findAllOrders();
    if (found == null || found.isEmpty()) {
      return ApiResponseDto.fail("주문 없음");
    }
    List<OrderResponse> result = found.stream().map(Order::toResponse).toList();
    return ApiResponseDto.success("주문 목록 전체 조회", result);
  }

  public ApiResponseDto<List<OrderResponse>> soldList() {
    String role = UserContext.get().role();
    String userId = UserContext.get().userId();

    List<Order> found = orderRepository.findAllOrdersBySellerId(userId);
    if (found == null || found.isEmpty()) {
      return ApiResponseDto.fail("sellerId에 해당하는 주문 없음");
    }
    List<OrderResponse> result = found.stream().map(Order::toResponse).toList();
    return ApiResponseDto.success("판매 목록 전체 조회", result);
  }

  public ApiResponseDto<List<OrderResponse>> boughtList() {

    String userId = UserContext.get().userId();

    List<Order> found = orderRepository.findAllOrdersByBuyerId(userId);
    if (found == null || found.isEmpty()) {
      return ApiResponseDto.fail("buyerId에 해당하는 주문 없음");
    }
    List<OrderResponse> result = found.stream().map(Order::toResponse).toList();
    return ApiResponseDto.success("구매 목록 전체 조회", result);
  }

  public ApiResponseDto<OrderResponse> findOne(String id) {
    Order found = orderRepository.findById(id).orElse(null);
    if (found == null) {
      return ApiResponseDto.fail("id에 해당하는 주문 없음");
    }
    return ApiResponseDto.success("주문 1건 조회", found.toResponse());
  }

  public ApiResponseDto<OrderResponse> create(OrderRegisterRequest request) {

    String validate = validateRegisterParam(request);

    if (validate != null) {
      throw new CustomException(validate);
    }
    Order order = Order.fromRequest(request);
    Order result = orderRepository.save(order);
    return ApiResponseDto.success("주문 저장", result.toResponse());
  }

  public ApiResponseDto<OrderResponse> update(OrderModifyRequest request) {
    Order order = orderRepository.findById(request.id()).orElse(null);
    String validate = validateModifyParam(request);
    if (validate != null) {
      throw new CustomException(validate);
    }
    if (order == null) {
      return ApiResponseDto.fail("id에 해당하는 주문 없음");
    }
    order.setStatus(request.status());
    if (request.status() == OrderStatus.PAID) {
      order.setPayYn("Y");
      order.setPayCompleteDate(LocalDateTime.now());
    }
    order = orderRepository.save(order);
    return ApiResponseDto.success("주문 상태 업데이트", order.toResponse());
  }

  @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
  public List<SettlementRegisterRequest> findConfirmedOrders(int page, int pageSize) {

    LocalDateTime start = LocalDateTime.now().minusWeeks(3).with(LocalTime.MIN);
    LocalDateTime end = LocalDateTime.now().minusWeeks(2).with(LocalTime.MAX);
    log.info(">>> date range: {} ~ {}", start, end);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by("updatedAt").ascending());

    Page<Order> ordersPage = orderRepository.findAllByStatusAndUpdatedAtBetween(
        OrderStatus.CONFIRM_BUY,
        start,
        end,
        pageable
    );
    log.info(">>> ordersPage: {}", ordersPage);
    return ordersPage.stream().map(SettlementRegisterRequest::fromOrder).toList();
  }

  @Transactional
  public List<String> updateStatusBulk(OrderStatus oldStatus,
      OrderStatus newStatus, LocalDateTime targetDate
  ) {

    return orderRepository.updateStatusByUpdatedAtAndReturnBuyer(oldStatus, newStatus, targetDate);
  }

  private String validateRegisterParam(OrderRegisterRequest request) {
    if (request.sellerId() == null || request.sellerId().isEmpty()) {
      return "판매자 id가 필요합니다.";
    }
    if (request.buyerId() == null || request.buyerId().isEmpty()) {
      return "구매자 id가 필요합니다.";
    }
    if (request.auctionId() == null || request.auctionId().isEmpty()) {
      return "경매 id가 필요합니다.";
    }
    if (request.winningAmount() == null) {
      return "낙찰가가 필요합니다.";
    }
    if (request.winningDate() == null) {
      return "낙찰이 확정된 일자가 필요합니다.";
    }

    return null;
  }

  private String validateModifyParam(OrderModifyRequest request) {
    if (request.id() == null || request.id().isEmpty()) {
      return "id가 필요합니다.";
    }
    if (request.status() == null) {
      return "변경할 상태가 필요합니다.";
    }
    return null;
  }
}
