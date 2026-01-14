package com.dev_high.batch;

import com.dev_high.auction.application.dto.AuctionProductProjection;
import com.dev_high.auction.domain.*;
import com.dev_high.auction.infrastructure.bid.AuctionParticipationJpaRepository;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionCreateOrderRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionDepositRefundRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionStartEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.NotificationCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchHelper {

    private final AuctionRepository auctionRepository;
    private final AuctionParticipationJpaRepository auctionParticipationJpaRepository;
    private final KafkaEventPublisher eventPublisher;


    public RepeatStatus startAuctionsUpdate(StepContribution stepContribution,
                                            ChunkContext chunkContext) {

        List<AuctionProductProjection> targetIds = auctionRepository
                .bulkUpdateStartStatus();

        List<String> auctionIds = targetIds.stream()
                .map(AuctionProductProjection::getId)
                .distinct()

                .toList();
        List<String> productIds = targetIds.stream()
                .map(AuctionProductProjection::getProductId)
                .distinct()

                .toList();
        log.info("auction start target>> {}", targetIds.size());

        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        ec.put("startAuctionIds", auctionIds);
        ec.put("startProductIds", productIds);

        return RepeatStatus.FINISHED;
    }

    public RepeatStatus startAuctionsPostProcessing(StepContribution stepContribution,
                                                    ChunkContext chunkContext) {

        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        List<String> auctionIds =
                (List<String>) ec.get("startAuctionIds");

        List<String> productIds =
                (List<String>) ec.get("startProductIds");

        if (auctionIds != null && !auctionIds.isEmpty()) {

            List<Auction> auctions = auctionRepository.findByIdIn(auctionIds);

            // 여기서 알림, 후처리 로직 실행 ex) 해당 상품찜한 유저에게알림 발송

            for (Auction auction : auctions) {


                sendSearchUpdateEvent(auction);

                try {
                    eventPublisher.publish(
                            KafkaTopics.AUCTION_START_EVENT,
                            new AuctionStartEvent(auction.getProductId(), auction.getId()));
                } catch (Exception e) {
                    // pass
                    log.error("kafka send failed: {}", e);
                }


            }
        }
        ec.remove("startAuctionIds");
        ec.remove("startProductIds");

        return RepeatStatus.FINISHED;
    }


    public RepeatStatus endAuctionsUpdate(StepContribution stepContribution,
                                          ChunkContext chunkContext) {
        List<AuctionProductProjection> targetIds = auctionRepository
                .bulkUpdateEndStatus();

        List<String> auctionIds = targetIds.stream()
                .map(AuctionProductProjection::getId)
                .distinct()
                .toList();

        List<String> productIds = targetIds.stream()
                .map(AuctionProductProjection::getProductId)
                .distinct()
                .toList();

        log.info("auction end target>> {}", targetIds.size());
        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        ec.put("endAuctionIds", auctionIds);
        ec.put("endProductIds", productIds);

        return RepeatStatus.FINISHED;

    }


    public RepeatStatus endAuctionsPostProcessing(StepContribution stepContribution,
                                                  ChunkContext chunkContext) {

        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        List<String> auctionIds =
                (List<String>) ec.get("endAuctionIds");

        List<String> productIds =
                (List<String>) ec.get("endProductIds");

        if (auctionIds != null && !auctionIds.isEmpty()) {

            for (String auctionId : auctionIds) {
                process(auctionId);
            }
        }
        ec.remove("endAuctionIds");
        ec.remove("endProductIds");
        return RepeatStatus.FINISHED;
    }

    public void process(String targetId) {

        Auction auction = auctionRepository.findById(targetId).orElse(null);
        AuctionLiveState state = auction.getLiveState();
        if (auction == null || state == null) {
            return;
        }
        String sellerId = auction.getCreatedBy();

        String highestUserId = state.getHighestUserId();

        if (highestUserId == null) {
            // 유찰 처리
            auction.changeStatus(AuctionStatus.FAILED, "SYSTEM");
            try {
                // 판매자에게 알림
                eventPublisher.publish(
                        KafkaTopics.NOTIFICATION_REQUEST,
                        new NotificationRequestEvent(
                                List.of(sellerId),
                                "경매가 유찰되었습니다.",
                                "/auctions/" + auction.getId(),
                                NotificationCategory.Type.AUCTION_NO_BID));
            } catch (Exception e) {
                log.error("kafka send failed :{}", e);
            }

        } else {



            /* TODO: chunk */
            List<String> userIds = auctionParticipationJpaRepository.findByAuctionId(
                    targetId).stream().map(AuctionParticipation::getUserId).toList();

            // 이벤트 발행
            if (!userIds.isEmpty()) {

                try {
                    eventPublisher.publish(
                            KafkaTopics.NOTIFICATION_REQUEST,
                            new NotificationRequestEvent(
                                    userIds,
                                    " 경매가 종료되었습니다.",
                                    "/auctions/" + auction.getId(),
                                    NotificationCategory.Type.AUCTION_CLOSED));

                } catch (Exception e) {
                    log.error("경매 종료 알림 실패: auctionId={}", targetId, e);

                }

                try {
                    // deposit service에 해당 유저들 환불요청 (kafka or rest)
                    // highestUserId 은 제외하고 환불요청을함.
                    List<String> refundUserIds = userIds.stream()
                            .filter(id -> !id.equals(highestUserId))
                            .toList();
                    eventPublisher.publish(KafkaTopics.AUCTION_DEPOSIT_REFUND_REQUESTED,
                            new AuctionDepositRefundRequestEvent(refundUserIds, targetId,
                                    auction.getDepositAmount()));

                } catch (Exception e) {
                    log.error("환불 요청 실패: auctionId={}", targetId, e);

                }
            }

            try {
                BigDecimal bid = state.getCurrentBid();

                // 주문생성 요청 kafka  ,
                eventPublisher.publish(
                        KafkaTopics.AUCTION_ORDER_CREATED_REQUESTED,
                        new AuctionCreateOrderRequestEvent(targetId, auction.getProductId(),auction.getProductName(), highestUserId, sellerId,
                                bid, auction.getDepositAmount(), OffsetDateTime.now()));
            } catch (Exception e) {
                log.error("주문 이벤트 발행 실패: auctionId={}", targetId, e);
            }
        }

        sendSearchUpdateEvent(auction);

    }

    /*TODO: es 경매/상품 따로 인덱싱 */
    private void sendSearchUpdateEvent(Auction auction) {

        String productId = auction.getProductId();


//        eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED,
//                new AuctionUpdateSearchRequestEvent(
//                        auction.getId(), productId,
//                        auction.getStartBid(), auction.getDepositAmount(), auction.getStatus().name(),
//                         auction.getAuctionStartAt(), auction.getAuctionEndAt()
//                ));
    }

}