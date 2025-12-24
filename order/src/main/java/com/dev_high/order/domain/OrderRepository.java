package com.dev_high.order.domain;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findAllOrders();

    Order save(Order order);

    List<Order> findAllOrdersBySellerIdOrderByUpdatedAtDesc(String sellerId);

    List<Order> findAllOrdersByBuyerIdOrderByUpdatedAtDesc(String buyerId);

    Optional<Order> findById(String id);

    Page<Order> findAllByStatusAndUpdatedAtBetween(
            OrderStatus status,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );

    List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(OrderStatus oldStatus,
                                                                      OrderStatus newStatus,
                                                                      OffsetDateTime targetDate);

    Long getStatusCount(String buyerId, OrderStatus status);

    List<Order> findByBuyerIdAndStatusOrderByUpdatedAtDesc(String buyerId, OrderStatus status);

    List<Order> findBySellerIdAndStatusOrderByUpdatedAtDesc(String sellerId, OrderStatus status);


}
