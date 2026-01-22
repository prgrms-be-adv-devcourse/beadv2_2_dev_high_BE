package com.dev_high.settlement.order.infrastructure;

import com.dev_high.settlement.order.application.dto.UpdateOrderProjection;
import com.dev_high.settlement.order.domain.WinningOrder;
import com.dev_high.settlement.order.domain.OrderRepository;
import com.dev_high.settlement.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderRepository;

    @Override
    public List<WinningOrder> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public WinningOrder save(WinningOrder order) {
        return orderRepository.save(order);
    }

    @Override
    public List<WinningOrder> findAllOrdersBySellerIdOrderByUpdatedAtDesc(String sellerId) {
        return orderRepository.findAllOrdersBySellerIdOrderByUpdatedAtDesc(sellerId);
    }

    @Override
    public List<WinningOrder> findAllOrdersByBuyerIdOrderByUpdatedAtDesc(String buyerId) {
        return orderRepository.findAllOrdersByBuyerIdOrderByUpdatedAtDesc(buyerId);
    }

    @Override
    public Optional<WinningOrder> findById(String id) {
        return orderRepository.findById(id);
    }

    @Override
    public Page<WinningOrder> findAllByStatusAndUpdatedAtBetween(OrderStatus status, OffsetDateTime start,
                                                                 OffsetDateTime end, Pageable pageable) {
        return orderRepository.findAllByStatusAndUpdatedAtBetween(status, start, end, pageable);
    }

    @Override
    public List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(OrderStatus oldStatus,
                                                                             OrderStatus newStatus,
                                                                             OffsetDateTime targetDate) {
        return orderRepository.updateStatusByUpdatedAtAndReturnBuyer(oldStatus.name(), newStatus.name(),
                targetDate);
    }

    @Override
    public Long getStatusCount(String buyerId, OrderStatus status) {

        return orderRepository.countByBuyerIdAndStatus(buyerId, status);
    }

    @Override
    public List<WinningOrder> findByBuyerIdAndStatusOrderByUpdatedAtDesc(String buyerId, OrderStatus status) {
        return orderRepository.findByBuyerIdAndStatusOrderByUpdatedAtDesc(buyerId, status);
    }

    @Override
    public List<WinningOrder> findBySellerIdAndStatusOrderByUpdatedAtDesc(String sellerId, OrderStatus status) {
        return orderRepository.findBySellerIdAndStatusOrderByUpdatedAtDesc(sellerId, status);
    }


}
