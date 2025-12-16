package com.dev_high.order.domain;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

  List<Order> findAllOrders();

  Order save(Order order);

  List<Order> findAllOrdersBySellerId(String sellerId);

  List<Order> findAllOrdersByBuyerId(String buyerId);

  Optional<Order> findById(String id);

  Page<Order> findAllByStatusAndUpdatedAtBetween(
      OrderStatus status,
      LocalDateTime start,
      LocalDateTime end,
      Pageable pageable
  );

  List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(OrderStatus oldStatus,
      OrderStatus newStatus,
      LocalDateTime targetDate);
}
