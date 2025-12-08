package com.dev_high.order.infrastructure;

import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    List<Order> findAllOrdersBySellerId(String sellerId);

    List<Order> findAllOrdersByBuyerId(String buyerId);

    List<Order> findAllOrdersByPayYnAndCreatedAtBetween(String payYn, LocalDateTime payCompleteDateStart, LocalDateTime payCompleteDateEnd);

    List<Order> findAllByStatusAndPayCompleteDateBetween(OrderStatus status, LocalDateTime payCompleteDate, LocalDateTime payCompleteDate2);
}
