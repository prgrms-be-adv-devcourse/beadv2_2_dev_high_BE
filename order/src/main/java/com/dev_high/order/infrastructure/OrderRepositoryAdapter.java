package com.dev_high.order.infrastructure;

import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
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
    public List<Order> findAllOrdersBySellerId(String sellerId) {
        return orderRepository.findAllOrdersBySellerId(sellerId);
    }

    @Override
    public List<Order> findAllOrdersByBuyerId(String buyerId) {
        return orderRepository.findAllOrdersByBuyerId(buyerId);
    }

    @Override
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findAllByPayCompleteDateAndStatus(LocalDateTime payCompleteDateStart, LocalDateTime payCompleteDateEnd, OrderStatus status) {
        return orderRepository.findAllByStatusAndPayCompleteDateBetween(status, payCompleteDateStart, payCompleteDateEnd);    }
}
