package com.dev_high.order.domain;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {


    WinningOrder save(WinningOrder order);

    Page<WinningOrder> findAllOrdersBySellerId(String sellerId ,Pageable pageable);

    Page<WinningOrder> findAllOrdersByBuyerId(String buyerId ,Pageable pageable);

    Optional<WinningOrder> findById(String id);

    Page<WinningOrder> findAllByStatusAndUpdatedAtBetween(
            OrderStatus status,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );

    List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(OrderStatus oldStatus,
                                                                      OrderStatus newStatus,
                                                                      OffsetDateTime targetDate);

    List<UpdateOrderProjection> updateStatusByPaymentLimitDateAndReturnBuyer(OrderStatus oldStatus,
                                                                             OrderStatus newStatus,
                                                                             OffsetDateTime targetDate);

    Long getStatusCount(String buyerId, OrderStatus status);

    Page<WinningOrder> findByBuyerIdAndStatus(String buyerId, OrderStatus status ,Pageable pageable);

    Page<WinningOrder> findBySellerIdAndStatus(String sellerId, OrderStatus status ,Pageable pageable);

    boolean existsByAuctionIdAndStatus(String auctionId, OrderStatus status);

    List<WinningOrder> findWinningOrdersForRecommendation(
        List<String> productIds,
        OffsetDateTime winningDate,
        Pageable pageable
    );



}
