package com.dev_high.order.infrastructure;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderRepository;

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findAllOrdersBySellerIdOrderByUpdatedAtDesc(String sellerId) {
        return orderRepository.findAllOrdersBySellerIdOrderByUpdatedAtDesc(sellerId);
    }

    @Override
    public List<Order> findAllOrdersByBuyerIdOrderByUpdatedAtDesc(String buyerId) {
        return orderRepository.findAllOrdersByBuyerIdOrderByUpdatedAtDesc(buyerId);
    }

    @Override
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    public Page<Order> findAllByStatusAndUpdatedAtBetween(OrderStatus status, LocalDateTime start,
                                                          LocalDateTime end, Pageable pageable) {
        return orderRepository.findAllByStatusAndUpdatedAtBetween(status, start, end, pageable);
    }

    @Override
    public List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(OrderStatus oldStatus,
                                                                             OrderStatus newStatus,
                                                                             LocalDateTime targetDate) {
        return orderRepository.updateStatusByUpdatedAtAndReturnBuyer(oldStatus.name(), newStatus.name(),
                targetDate);
    }


}
