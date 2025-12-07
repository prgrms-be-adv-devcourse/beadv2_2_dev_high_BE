package com.dev_high.order.infrastructure;

import com.dev_high.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, String> {
    List<Order> findAllOrdersBySellerId(String sellerId);

    List<Order> findAllOrdersByBuyerId(String buyerId);
}
