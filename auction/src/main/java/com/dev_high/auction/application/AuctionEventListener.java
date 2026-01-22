package com.dev_high.auction.application;

import com.dev_high.auction.domain.AuctionRepository;
import com.dev_high.auction.domain.AuctionStatus;
import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.event.deposit.DepositCompletedEvent;
import com.dev_high.common.kafka.event.order.OrderToAuctionUpdateEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.NetworkException;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@Slf4j
@Lazy(false)
public class AuctionEventListener {

    private final KafkaEventPublisher eventPublisher;

    private final BidRecordService recordService;

    private final AuctionRepository auctionRepository;


    // 경매 참여자들의 보증금 환급 후 처리
    @KafkaListener(topics = KafkaTopics.DEPOSIT_AUCTION_REFUND_RESPONSE)
    @Transactional
    public void refundComplete(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

        DepositCompletedEvent val = JsonUtil.fromPayload(envelope.payload(),
                DepositCompletedEvent.class);

        try {
            if (val.type().equals("REFUND")) {
                recordService.markDepositRefunded(val.auctionId(), val.userIds());

            }
        } catch (TransientDataAccessException | NetworkException e) {
            // 일시적 오류: 재시도
            log.warn("일시적 오류 발생, 재시도: {}, 메시지: {}", e.getClass().getSimpleName(), envelope.payload());
            throw e;
        } catch (Exception e) {
            //재시도 없이 DLQ
            throw new RuntimeException();
        }

    }

//    @KafkaListener(topics = KafkaTopics.DEPOSIT_AUCTION_DEPOIST_RESPONSE) // 보증금 차감 이벤트
    @Deprecated
    public void depositComplete(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

        DepositCompletedEvent val = JsonUtil.fromPayload(envelope.payload(),
                DepositCompletedEvent.class);


        try {
            if (val.type().equals("DEPOSIT")) {
                val.userIds().forEach(id -> {
                    recordService.createParticipation(val.auctionId(), val.amount(), id);

                });
            }
        } catch (TransientDataAccessException | NetworkException e) {
            // 일시적 오류: 재시도
            log.warn("일시적 오류 발생: {}, 메시지: {}", e, envelope.payload());
            throw e;
        } catch (Exception e) {
            //재시도 없이 DLQ
            throw new RuntimeException();
        }




    }

    @KafkaListener(topics = KafkaTopics.ORDER_AUCTION_UPDATE)
    public void auctionStatusUpdate(KafkaEventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {

        OrderToAuctionUpdateEvent val = JsonUtil.fromPayload(envelope.payload(),
                OrderToAuctionUpdateEvent.class);



        try {
            auctionRepository.bulkUpdateStatus(val.auctionIds(),
                    (AuctionStatus.valueOf(val.status())));
        } catch (TransientDataAccessException | NetworkException e) {
            // 일시적 오류: 재시도
            log.warn("일시적 오류 발생: {}, 메시지: {}", e, envelope.payload());
            throw e;
        } catch (Exception e) {
            //재시도 없이 DLQ
            throw new RuntimeException();
        }



    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuctionUpdateSearchRequestEvent event) {
        eventPublisher.publish(KafkaTopics.AUCTION_SEARCH_UPDATED_REQUESTED, event);
    }


}
