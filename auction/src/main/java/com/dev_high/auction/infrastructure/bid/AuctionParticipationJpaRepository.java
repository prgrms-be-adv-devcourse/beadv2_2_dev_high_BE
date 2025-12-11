package com.dev_high.auction.infrastructure.bid;

import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionParticipationJpaRepository extends
    JpaRepository<AuctionParticipation, AuctionParticipationId> {

  // 특정 userId로 모든 참여 기록 조회
  List<AuctionParticipation> findByUserId(String userId);

  // 특정 auctionId의 전체 참여 기록 조회
  List<AuctionParticipation> findByAuctionId(String auctionId);

  // 특정 auctionId ,특정 userId
  List<AuctionParticipation> findByAuctionIdAndUserIdIn(String auctionId, List<String> userIds);

}
