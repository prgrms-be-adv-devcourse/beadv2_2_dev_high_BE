package com.dev_high.deposit.application.dto;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.deposit.application.DepositHistoryService;
import com.dev_high.deposit.application.DepositService;
import com.dev_high.deposit.domain.DepositType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*
 * 예치금 서비스 Kafka 리스너
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepositEventListener {
    private final DepositService depositService;
    private final DepositHistoryService depositHistoryService; // 의존성 추가

    /*
    * user 서비스에서 발행한 메시지
    * topics "user-deposit-create" 토픽 구독
    * */
    @KafkaListener(topics = "user-deposit-create")
    public void handleUserDepositCreate(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        try {
            Map<String, Object> payload = envelope.payload();
            String userId = (String) payload.get("userId");
            if (userId == null) {
                log.warn("User ID is null for user deposit create event: {}", envelope);
                return;
            }

            DepositCreateCommand command = new DepositCreateCommand(userId);

            depositService.createDepositAccount(command);
        } catch (Exception e) {
            log.error("Error processing user deposit create event: {}", envelope, e);
        }
    }

    /**
     * 경매 서비스에서 발행한 예치금 환불 요청 메시지를 처리합니다.
     */
    @KafkaListener(topics = "auction-deposit-refund-requested")
    public void handleDepositRefund(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        try {
            Map<String, Object> payload = envelope.payload();
            // 1. payload에서 데이터를 가져옵니다.
            List<String> userIds = (List<String>) payload.get("userIds");
            String auctionId = (String) payload.get("auctionId");
            // Kafka는 보통 Double로 숫자를 역직렬화하므로, Double을 거쳐 BigDecimal로 변환합니다.
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            // userIds가 null이거나 비어있으면 아무것도 하지 않고 종료
            if (userIds == null || userIds.isEmpty()) {
                log.warn("User ID list is empty for refund event: {}", envelope);
                return;
            }

            log.info("Processing deposit refund for auction [{}], amount [{}], for {} users.", auctionId, amount, userIds.size());

            userIds.forEach(userId -> {
                DepositHistoryCreateCommand command = new DepositHistoryCreateCommand(
                        userId,
                        auctionId, // auctionId를 depositOrderId 필드에 저장하여 추적
                        DepositType.REFUND,
                        amount.longValue() // BigDecimal을 long으로 변환
                );
                depositHistoryService.createHistory(command);
            });

        } catch (Exception e) {
            log.error("Error processing deposit refund event: {}", envelope, e);
        }
    }
}
