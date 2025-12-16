package com.dev_high.auction.application;

import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.auction.AuctionCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionProductUpdateEvent;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.event.deposit.DepositCompletedEvent;
import com.dev_high.common.kafka.event.order.OrderToAuctionUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventListener {

    private final KafkaEventPublisher eventPublisher;

    private final BidRecordService recordService;

    private final AuctionRepository auctionRepository;


    // 경매 참여자들의 보증금 환급 후 처리
    @KafkaListener(topics = KafkaTopics.DEPOSIT_AUCTION_REFUND_RESPONSE)
    public void refundComplete(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

        DepositCompletedEvent val = JsonUtil.fromPayload(envelope.payload(),
                DepositCompletedEvent.class);

        try {
            if (val.type().equals("REFUND")) {
                recordService.markDepositRefunded(val.auctionId(), val.userIds());
            }
        } catch (Exception e) {

        }

    }

    @KafkaListener(topics = KafkaTopics.DEPOSIT_AUCTION_DEPOIST_RESPONSE) // 보증금 차감 이벤트
    public void depositComplete(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

        DepositCompletedEvent val = JsonUtil.fromPayload(envelope.payload(),
                DepositCompletedEvent.class);

        if (val.type().equals("DEPOSIT")) {
            val.userIds().forEach(id -> {
                recordService.createParticipation(val.auctionId(), val.amount(), id);

            });
        }

    }

    @KafkaListener(topics = KafkaTopics.ORDER_AUCTION_UPDATE)
    public void auctionStatusUpdate(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

        OrderToAuctionUpdateEvent val = JsonUtil.fromPayload(envelope.payload(),
                OrderToAuctionUpdateEvent.class);
        // 미결제로 인한 주문취소 이벤트 소비하고 상품에 상태를 전달

        List<String> productIds = auctionRepository.bulkUpdateStatus(val.auctionIds(),
                (AuctionStatus.valueOf(val.status())));
        try {
            eventPublisher.publish(KafkaTopics.AUCTION_PRODUCT_UPDATE,
                    new AuctionProductUpdateEvent(productIds,
                            val.status()));
        } catch (Exception e) {

        }

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuctionCreateSearchRequestEvent event) {
        eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_CREATED_REQUESTED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuctionUpdateSearchRequestEvent event) {
        eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(String event) {
        eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_DELETED_REQUESTED, event);
    }


}
