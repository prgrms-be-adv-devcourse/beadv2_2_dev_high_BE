package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionParticipationResponse;
import com.dev_high.auction.domain.AuctionBidHistory;
import com.dev_high.auction.domain.AuctionLiveState;
import com.dev_high.auction.domain.AuctionParticipation;
import com.dev_high.auction.domain.BidType;
import com.dev_high.auction.domain.idclass.AuctionParticipationId;
import com.dev_high.auction.exception.AlreadyWithdrawnException;
import com.dev_high.auction.exception.AuctionParticipationNotFoundException;
import com.dev_high.auction.exception.CannotWithdrawHighestBidderException;
import com.dev_high.auction.infrastructure.bid.AuctionBidHistoryJpaRepository;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.util.HttpUtil;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidRecordService {

  private final AuctionBidHistoryJpaRepository auctionBidHistoryJpaRepository;
  private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;

  private final RestTemplate restTemplate;
  private final EntityManager entityManager;

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
    if (userId == null || auctionId == null) {
      return false;
    }

    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    return auctionParticipationJpaRepository.existsById(participationId);
  }


  public AuctionParticipationResponse findParticipation(String auctionId) {

    String userId = UserContext.get().userId();

    boolean isExist = hasParticipated(userId, auctionId);

    if (!isExist) {

      return AuctionParticipationResponse.isNotParticipated(BigDecimal.ZERO);
    }

    AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
    AuctionParticipation participation = auctionParticipationJpaRepository.findById(participationId)
        .orElseThrow(AuctionParticipationNotFoundException::new);

    return AuctionParticipationResponse.isParticipated(participation);

  }

  // 포기 처리
  // 보증금 즉시환불,,

  @Transactional
  public AuctionParticipationResponse withdrawAuctionBid(String auctionId) {
    String userId = UserContext.get().userId();
    AuctionParticipation participation = auctionParticipationJpaRepository.findById(
            new AuctionParticipationId(userId, auctionId))
        .orElseThrow(AuctionParticipationNotFoundException::new);

    if ("Y".equals(participation.getWithdrawnYn())) {
      throw new AlreadyWithdrawnException();
    }
    AuctionLiveState liveState = participation.getAuction().getLiveState();

    if (liveState.getHighestUserId().equals(userId)) {
      throw new CannotWithdrawHighestBidderException();
    }

    participation.markWithdraw();
    entityManager.flush();

    recordHistory(new AuctionBidHistory(auctionId, BigDecimal.ZERO, userId, BidType.BID_WITHDRAW));

    try {

      HttpHeaders headers = HttpUtil.createBearerHttpHeaders(UserContext.get().token());

      HttpEntity<Void> entity = new HttpEntity<>(headers);
      //임시
      String url = "http://APIGATEWAY/api/v1/deposit/" + userId;

      ResponseEntity<ApiResponseDto<?>> response;
      response = restTemplate.exchange(
          url,
          HttpMethod.POST,
          entity,
          new ParameterizedTypeReference<ApiResponseDto<?>>() {
          }
      );
      if (response.getBody() != null) {
        log.info("deposit response >>>{}", response.getBody().toString());
        processRefundComplete(participation);

      }
    } catch (Exception e) {
      log.error("보증금 환급 실패: {}", e);
    }
    return AuctionParticipationResponse.isParticipated(participation);

  }


  // 환불완료
  public void markDepositRefunded(String auctionId, List<String> userIds) {

    List<AuctionParticipation> participations = auctionParticipationJpaRepository.findByAuctionIdAndUserIdIn(
        auctionId,
        userIds);

    for (AuctionParticipation participation : participations) {
      // 환불 완료로 상태 변경
      processRefundComplete(participation);

    }

  }

  private void processRefundComplete(AuctionParticipation participation) {
    participation.markDepositRefunded();

    // 로그 기록
    recordHistory(new AuctionBidHistory(participation.getAuctionId(),
        participation.getDepositAmount(),
        participation.getUserId(),
        BidType.REFUND_COMPLETE));

  }


  public List<AuctionParticipation> getAllMyParticipations() {
    String userId = UserContext.get().userId();

    return auctionParticipationJpaRepository.findByUserId(userId);
  }

  public AuctionParticipationResponse createParticipation(String auctionId, BigDecimal decimal) {
    String userId = UserContext.get().userId();
    AuctionParticipationId id = new AuctionParticipationId(userId, auctionId);

    recordHistory(new AuctionBidHistory(auctionId, decimal, userId, BidType.DEPOSIT_SUCCESS));

    AuctionParticipation participation = auctionParticipationJpaRepository.save(
        new AuctionParticipation(id, decimal));
    auctionParticipationJpaRepository.save(participation);
    return AuctionParticipationResponse.isParticipated(participation);
  }
}
