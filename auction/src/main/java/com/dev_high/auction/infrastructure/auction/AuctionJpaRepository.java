package com.dev_high.auction.infrastructure.auction;

import com.dev_high.auction.application.dto.AuctionProductProjection;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionJpaRepository extends JpaRepository<Auction, String> {

    boolean existsByProductIdAndStatusInAndDeletedYn(String productId, List<AuctionStatus> statuses ,String deletedYn);

    List<Auction> findByProductIdAndDeletedYnOrderByIdDesc(String productId, String deletedYn);


    /* postgre 에서는 retuning 으로 id바로 조회가능 */
    @Modifying
    @Query(value = """
            UPDATE auction.auction
            SET status = 'IN_PROGRESS',
                updated_at = NOW(),
                updated_by = 'SYSTEM'
            WHERE status = 'READY'
              AND auction_start_at <= NOW()
            RETURNING
                id,
                product_id AS productId
            """, nativeQuery = true)
    List<AuctionProductProjection> bulkUpdateStart();

    @Modifying
    @Query(value = """
              UPDATE auction.auction
              SET status = 'COMPLETED',
                  updated_at = NOW(),
                  updated_by = 'SYSTEM'
              WHERE status = 'IN_PROGRESS' AND auction_end_at <= NOW()
              RETURNING
                id,
                product_id AS productId
            """, nativeQuery = true)
    List<AuctionProductProjection> bulkUpdateEnd();


    @Modifying
    @Query(value = """
              UPDATE auction.auction
            SET status = :status,
                updated_at = NOW(),
                updated_by = 'SYSTEM'
            WHERE id IN (:auctionIds)
            RETURNING product_id
            """, nativeQuery = true)
    List<String> bulkUpdateStatus(
            @Param("auctionIds") List<String> auctionIds,
            @Param("status") String status
    );
}
