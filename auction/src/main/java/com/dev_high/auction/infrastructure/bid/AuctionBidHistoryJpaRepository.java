package com.dev_high.auction.infrastructure.bid;

import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.BidType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionBidHistoryJpaRepository extends JpaRepository<AuctionBidHistory, Long> {

  Page<AuctionBidHistory> findByAuctionIdAndType(
      String auctionId,
      BidType type,
      Pageable pageable);

}
