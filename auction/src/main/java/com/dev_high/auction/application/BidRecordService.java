package com.dev_high.auction.application;

import com.dev_high.auction.application.dto.AuctionBidMessage;
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
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.HttpUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidRecordService {

    private final AuctionBidHistoryJpaRepository auctionBidHistoryJpaRepository;
    private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;

    private final RestTemplate restTemplate;
    private final EntityManager entityManager;
    private final KafkaEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuctionBidHistory recordHistory(AuctionBidHistory history) {
        return auctionBidHistoryJpaRepository.save(history);
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

            return AuctionParticipationResponse.isNotParticipated();
        }

        AuctionParticipationId participationId = new AuctionParticipationId(userId, auctionId);
        AuctionParticipation participation = auctionParticipationJpaRepository.findById(participationId)
                .orElseThrow(AuctionParticipationNotFoundException::new);

        return AuctionParticipationResponse.isParticipated(participation);

    }

    // 포기 처리
    // 보증금 즉시환불,,
    @Transactional
    public AuctionParticipationResponse withdrawAuctionBid(String auctionId, String userId) {

        AuctionParticipation participation = auctionParticipationJpaRepository.findById(
                        new AuctionParticipationId(userId, auctionId))
                .orElseThrow(AuctionParticipationNotFoundException::new);

        if ("Y".equals(participation.getWithdrawnYn())) {
            throw new AlreadyWithdrawnException();
        }
        AuctionLiveState liveState = participation.getAuction().getLiveState();

        if (userId.equals(liveState.getHighestUserId())) {
            throw new CannotWithdrawHighestBidderException();
        }

        participation.markWithdraw();
        entityManager.flush();

        recordHistory(new AuctionBidHistory(auctionId, BigDecimal.ZERO, userId, BidType.BID_WITHDRAW));

        depositUpdate(userId, auctionId, participation.getDepositAmount(), "REFUND");

        processRefundComplete(participation);


        return AuctionParticipationResponse.isParticipated(participation);

    }

    private void depositUpdate(String userId, String auctionId, BigDecimal amount, String type) {

        try {

            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("type", type);
            map.put("depositOrderId", auctionId);
            map.put("amount", amount);

            HttpEntity<Map<String, Object>> entity = HttpUtil.createGatewayEntity(map);

            String url = "http://APIGATEWAY/api/v1/deposit/usages";

            ResponseEntity<ApiResponseDto<?>> response;
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<ApiResponseDto<?>>() {
                    }
            );
            if (response.getBody() != null) {
                log.info("deposit response >>>{}", response.getBody().getData().toString());

            }
        } catch (Exception e) {
            log.error("보증금 환급 실패: {}", e);
        }
    }

    // 환불완료
    public long markDepositRefunded(String auctionId, List<String> userIds) {

        List<AuctionParticipation> participation = auctionParticipationJpaRepository.findByAuctionIdAndUserIdIn(
                auctionId,
                userIds);

        long count = 0;
        for (AuctionParticipation part : participation) {
            // 환불 완료로 상태 변경

            if (part.getDepositRefundedYn().equals("N")) {
                processRefundComplete(part);
                count++;
            }

        }

        return count;

    }

    private void processRefundComplete(AuctionParticipation participation) {
        participation.markDepositRefunded();

        // 로그 기록
        recordHistory(new AuctionBidHistory(participation.getAuctionId(),
                participation.getDepositAmount(),
                participation.getUserId(),
                BidType.REFUND_COMPLETE));

    }


    public List<AuctionParticipationResponse> getAllMyParticipation() {
        String userId = UserContext.get().userId();

        //TODO: 추가로 조회할 정보 (경매상태 ,최종낙찰여부)

        return auctionParticipationJpaRepository.findByUserId(userId).stream()
                .map(AuctionParticipationResponse::isParticipated).toList();
    }

    public AuctionParticipationResponse createParticipation(String auctionId, BigDecimal decimal, String userId) {


        if (hasParticipated(userId, auctionId)) {
            log.info("이미 처리되었습니다. : {}", auctionId);
            return null;
        }
        AuctionParticipationId id = new AuctionParticipationId(userId, auctionId);


        depositUpdate(userId, auctionId, decimal, "DEPOSIT");

        try {
            recordHistory(new AuctionBidHistory(auctionId, decimal, userId, BidType.DEPOSIT_SUCCESS));


            AuctionParticipation participation = auctionParticipationJpaRepository.save(
                    new AuctionParticipation(id, decimal));
            auctionParticipationJpaRepository.save(participation);
            return AuctionParticipationResponse.isParticipated(participation);

        } catch (Exception e) {
            eventPublisher.publish(KafkaTopics.AUCTION_DEPOSIT_REFUND_REQUESTED, new AuctionDepositRefundRequestEvent(List.of(userId), auctionId, decimal));
            throw e;
        }

    }


    public Page<AuctionBidMessage> getBidHistory(String auctionId, Pageable pageable) {
        Page<AuctionBidHistory> page = auctionBidHistoryJpaRepository
                .findByAuctionIdAndType(auctionId, BidType.BID_SUCCESS, pageable);

        // DTO 변환
        List<AuctionBidMessage> dtoList = page.getContent()
                .stream()
                .map(AuctionBidMessage::fromEntity)
                .toList();

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());

    }
}