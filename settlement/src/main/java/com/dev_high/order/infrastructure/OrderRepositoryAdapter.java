package com.dev_high.order.infrastructure;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
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
    public Page<WinningOrder> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }



    @Override
    public WinningOrder save(WinningOrder order) {
        return orderRepository.save(order);
    }

    @Override
    public Page<WinningOrder> findAllOrdersBySellerId(String sellerId ,Pageable pageable) {
        return orderRepository.findAllOrdersBySellerId(sellerId ,pageable);
    }

    @Override
    public Page<WinningOrder> findAllOrdersByBuyerId(String buyerId ,Pageable pageable) {
        return orderRepository.findAllOrdersByBuyerId(buyerId ,pageable);
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
    public Page<WinningOrder> findByBuyerIdAndStatus(String buyerId, OrderStatus status ,Pageable pageable) {
        return orderRepository.findByBuyerIdAndStatus(buyerId, status ,pageable);
    }

    @Override
    public Page<WinningOrder> findBySellerIdAndStatus(String sellerId, OrderStatus status ,Pageable pageable) {
        return orderRepository.findBySellerIdAndStatus(sellerId, status ,pageable);
    }

    @Override
    public List<WinningOrder> findWinningOrdersForRecommendation(
        List<String> productIds,
        OffsetDateTime winningDate,
        Pageable pageable
    ) {
        return orderRepository.findByProductIdInAndWinningDateAfter(
            productIds,
            winningDate,
            pageable
        );
    }


}
