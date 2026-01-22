package com.dev_high.settlement.domain.order;

import com.dev_high.settlement.application.order.dto.UpdateOrderProjection;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    List<WinningOrder> findAllOrders();

    WinningOrder save(WinningOrder order);

    List<WinningOrder> findAllOrdersBySellerIdOrderByUpdatedAtDesc(String sellerId);

    List<WinningOrder> findAllOrdersByBuyerIdOrderByUpdatedAtDesc(String buyerId);

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

    Long getStatusCount(String buyerId, OrderStatus status);

    List<WinningOrder> findByBuyerIdAndStatusOrderByUpdatedAtDesc(String buyerId, OrderStatus status);

    List<WinningOrder> findBySellerIdAndStatusOrderByUpdatedAtDesc(String sellerId, OrderStatus status);


}
