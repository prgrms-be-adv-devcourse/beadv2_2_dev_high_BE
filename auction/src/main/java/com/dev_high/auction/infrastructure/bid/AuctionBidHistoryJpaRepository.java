package com.dev_high.auction.infrastructure.bid;

import com.dev_high.auction.domain.AuctionBidHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionBidHistoryJpaRepository extends JpaRepository<AuctionBidHistory,Long> {

}
