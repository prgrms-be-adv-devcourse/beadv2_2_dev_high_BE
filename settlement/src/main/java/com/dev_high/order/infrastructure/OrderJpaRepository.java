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
                      AND deleted_yn = 'N' 
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

    @Modifying
    @Query(
            value = """
                    UPDATE "settlement"."winning_order"
                    SET
                      status = :newStatus,
                      updated_at = now()
                    WHERE status = :oldStatus
                      AND payment_limit_date <= :targetDate
                      AND deleted_yn = 'N'
                    RETURNING
                      id         AS id,
                      buyer_id   AS buyerId,
                      seller_id  AS sellerId,
                      auction_id AS auctionId,
                      winning_amount AS winningAmount
                    """,
            nativeQuery = true
    )
    List<UpdateOrderProjection> updateStatusByPaymentLimitDateAndReturnBuyer(
            @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus,
            @Param("targetDate") OffsetDateTime targetDate
    );

    Page<WinningOrder> findAllByStatusAndUpdatedAtBetweenAndDeletedYn(
            OrderStatus status,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable,
            String deletedYn
    );


    Long countByBuyerIdAndStatusAndDeletedYn(String buyerId, OrderStatus orderStatus ,String deletedYn);

    Long countBySellerIdAndStatusAndDeletedYn(String sellerId, OrderStatus orderStatus ,String deletedYn);

    Page<WinningOrder> findAllOrdersBySellerIdAndDeletedYn(String sellerId ,Pageable pageable, String deletedYn);

    Page<WinningOrder> findAllOrdersByBuyerIdAndDeletedYn(String buyerId ,Pageable pageable,String deletedYn);


    Page<WinningOrder> findByBuyerIdAndStatusAndDeletedYn(String buyerId, OrderStatus status ,Pageable pageable,String deletedYn);

    Page<WinningOrder> findBySellerIdAndStatusAndDeletedYn(String sellerId, OrderStatus status ,Pageable pageable,String deletedYn);

    boolean existsByAuctionIdAndStatusAndDeletedYn(String auctionId, OrderStatus status,String deletedYn);

    List<WinningOrder> findByProductIdInAndWinningDateAfterAndDeletedYn(
        List<String> productIds,
        OffsetDateTime winningDate,
        Pageable pageable,
        String deletedYn
    );


}
