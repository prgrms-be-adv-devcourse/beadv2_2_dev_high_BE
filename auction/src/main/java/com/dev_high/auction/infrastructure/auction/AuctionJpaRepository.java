package com.dev_high.auction.infrastructure.auction;

import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionJpaRepository extends JpaRepository<Auction ,String> {

  boolean existsByProductIdAndStatusIn(String productId, List<AuctionStatus> statuses);

  List<Auction> findByProduct_Id(String productId);


  /* postgre 에서는 retuning 으로 id바로 조회가능 */
  @Modifying
  @Query(value = """
    UPDATE auction.auction
    SET status = 'IN_PROGRESS',
        updated_at = NOW(),
        updated_by = 'SYSTEM'
    WHERE status = 'READY' AND auction_start_at <= NOW()
    RETURNING id
    """, nativeQuery = true)
  List<String> bulkUpdateStart();

  @Modifying
  @Query(value = """
    UPDATE auction.auction
    SET status = 'COMPLETED',
        updated_at = NOW(),
        updated_by = 'SYSTEM'
    WHERE status = 'IN_PROGRESS' AND auction_end_at <= NOW()
    RETURNING id
    """, nativeQuery = true)
  List<String> bulkUpdateEnd();
}
