package com.dev_high.order.infrastructure;

import com.dev_high.order.application.dto.UpdateOrderProjection;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderJpaRepository extends JpaRepository<WinningOrder, String> {


    @Modifying
    @Query(
            value = """
                    UPDATE "settlement"."winning_order"
                    SET
                      status = :newStatus,
                      updated_at = now()
                    WHERE status = :oldStatus
                      AND updated_at <= :targetDate
                    RETURNING
                      id         AS id,
                      buyer_id   AS buyerId,
                      seller_id  AS sellerId,
                      auction_id AS auctionId,
                      winning_amount AS winningAmount
                    """,
            nativeQuery = true
    )
    List<UpdateOrderProjection> updateStatusByUpdatedAtAndReturnBuyer(
            @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus,
            @Param("targetDate") OffsetDateTime targetDate
    );

    Page<WinningOrder> findAllByStatusAndUpdatedAtBetween(
            OrderStatus status,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable
    );


    Long countByBuyerIdAndStatus(String buyerId, OrderStatus orderStatus);

    Long countBySellerIdAndStatus(String sellerId, OrderStatus orderStatus);

    Page<WinningOrder> findAllOrdersBySellerId(String sellerId ,Pageable pageable);

    Page<WinningOrder> findAllOrdersByBuyerId(String buyerId ,Pageable pageable);


    Page<WinningOrder> findByBuyerIdAndStatus(String buyerId, OrderStatus status ,Pageable pageable);

    Page<WinningOrder> findBySellerIdAndStatus(String sellerId, OrderStatus status ,Pageable pageable);

}
