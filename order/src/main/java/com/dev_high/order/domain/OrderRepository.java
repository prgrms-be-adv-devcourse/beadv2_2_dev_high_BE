package com.dev_high.order.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findAllOrders();
    Order save(Order order);
    List<Order> findAllOrdersBySellerId(String sellerId);
    List<Order> findAllOrdersByBuyerId(String buyerId);
    Optional<Order> findById(String id);
    List<Order> findAllByPayCompleteDateAndStatus(LocalDateTime payCompleteDateStart, LocalDateTime payCompleteDateEnd, OrderStatus status);
}
