package com.dev_high.order.infrastructure;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, String> {


    @Modifying
    @Query(
            value = """
                    UPDATE "order"."order"
                    SET
                      status = :newStatus,
                      updated_at = now()
                    WHERE status = :oldStatus
                      AND updated_at <= :targetDate
                    RETURNING
                      buyer_id   AS buyerId,
                      seller_id  AS sellerId,
                      auction_id AS auctionId
                    """,
            nativeQuery = true
    )
    List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(
            @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus,
            @Param("targetDate") OffsetDateTime targetDate
    );


    Page<Order> findAllByStatusAndUpdatedAtBetween(
            OrderStatus status,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );

    Long countByBuyerIdAndStatus(String buyerId, OrderStatus orderStatus);

    Long countBySellerIdAndStatus(String sellerId, OrderStatus orderStatus);

    List<Order> findAllOrdersBySellerIdOrderByUpdatedAtDesc(String sellerId);

    List<Order> findAllOrdersByBuyerIdOrderByUpdatedAtDesc(String buyerId);


    List<Order> findByBuyerIdAndStatusOrderByUpdatedAtDesc(String buyerId, OrderStatus status);

    List<Order> findBySellerIdAndStatusOrderByUpdatedAtDesc(String sellerId, OrderStatus status);

}
