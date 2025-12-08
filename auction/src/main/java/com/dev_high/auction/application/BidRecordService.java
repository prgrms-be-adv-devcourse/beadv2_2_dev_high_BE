package com.dev_high.auction.application;

import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.auction.infrastructure.bid.AuctionBidHistoryJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BidRecordService {

  private final AuctionBidHistoryJpaRepository auctionBidHistoryJpaRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordHistory(AuctionBidHistory history) {
    auctionBidHistoryJpaRepository.save(history);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveParticipation(AuctionParticipation participation) {
    auctionParticipationJpaRepository.save(participation);
  }

  /**
   * 특정 경매에 참여한 기록이 있는지 체크
   */
  public boolean hasParticipated(String userId, String auctionId) {
    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    return auctionParticipationJpaRepository.existsById(participationId);
  }

  public boolean existspParticipation(String userId, String auctionId) {
    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    return auctionParticipationJpaRepository.existsById(participationId);
  }

  public Optional<AuctionParticipation> findParticipation(String userId, String auctionId) {

    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    return auctionParticipationJpaRepository.findById(participationId);
  }

  public AuctionParticipation getOrCreateParticipation(String userId, String auctionId) {

    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    return auctionParticipationJpaRepository.findById(participationId)
        .orElseGet(() -> {
          AuctionParticipation participation = new AuctionParticipation(participationId);
          participation.placeBid(BigDecimal.ZERO); // 초기 입찰가
          return participation;
        });
  }

  public List<AuctionParticipation> getAllMyParticipations() {
    String userId = "TEST";

    return auctionParticipationJpaRepository.findByUserId(userId);
  }

}
