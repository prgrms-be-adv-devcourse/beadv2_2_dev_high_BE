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
    public Optional<WinningOrder> findById(String id) {
        return orderRepository.findById(id);
    }
    
    @Override
    public WinningOrder save(WinningOrder order) {
        return orderRepository.save(order);
    }

    @Override
    public Page<WinningOrder> findAllOrdersBySellerId(String sellerId ,Pageable pageable) {

        return orderRepository.findAllOrdersBySellerIdAndDeletedYn(sellerId ,pageable ,"N");
    }

    @Override
    public Page<WinningOrder> findAllOrdersByBuyerId(String buyerId ,Pageable pageable) {
        return orderRepository.findAllOrdersByBuyerIdAndDeletedYn(buyerId ,pageable,"N");
    }


    @Override
    public Page<WinningOrder> findAllByStatusAndUpdatedAtBetween(OrderStatus status, OffsetDateTime start,
                                                                 OffsetDateTime end, Pageable pageable) {
        return orderRepository.findAllByStatusAndUpdatedAtBetweenAndDeletedYn(status, start, end, pageable,"N");
    }

    @Override
    public List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(OrderStatus oldStatus,
                                                                             OrderStatus newStatus,
                                                                             OffsetDateTime targetDate) {
        return orderRepository.updateStatusByUpdatedAtAndReturnBuyer(oldStatus.name(), newStatus.name(),
                targetDate);
    }

    @Override
    public List<UpdateOrderProjection> updateStatusByPaymentLimitDateAndReturnBuyer(
            OrderStatus oldStatus,
            OrderStatus newStatus,
            OffsetDateTime targetDate
    ) {
        return orderRepository.updateStatusByPaymentLimitDateAndReturnBuyer(
                oldStatus.name(),
                newStatus.name(),
                targetDate
        );
    }

    @Override
    public Long getStatusCount(String buyerId, OrderStatus status) {

        return orderRepository.countByBuyerIdAndStatusAndDeletedYn(buyerId, status,"N");
    }

    @Override
    public Page<WinningOrder> findByBuyerIdAndStatus(String buyerId, OrderStatus status ,Pageable pageable) {
        return orderRepository.findByBuyerIdAndStatusAndDeletedYn(buyerId, status ,pageable,"N");
    }

    @Override
    public Page<WinningOrder> findBySellerIdAndStatus(String sellerId, OrderStatus status ,Pageable pageable) {
        return orderRepository.findBySellerIdAndStatusAndDeletedYn(sellerId, status ,pageable,"N");
    }

    @Override
    public boolean existsByAuctionIdAndStatus(String auctionId, OrderStatus status) {
        return orderRepository.existsByAuctionIdAndStatusAndDeletedYn(auctionId, status,"N");
    }

    @Override
    public List<WinningOrder> findWinningOrdersForRecommendation(
        List<String> productIds,
        OffsetDateTime winningDate,
        Pageable pageable
    ) {

        return orderRepository.findByProductIdInAndWinningDateAfterAndDeletedYn(
            productIds,
            winningDate,
            pageable,
                "N"
        );
    }

    @Override
    public WinningOrder findByPurchaseOrderId(String purchaseId) {
        return orderRepository.findByPurchaseOrderId(purchaseId);
    }


}
