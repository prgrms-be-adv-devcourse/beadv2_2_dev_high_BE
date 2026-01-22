package com.dev_high.user.deposit.infrastructure.kafka;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.deposit.DepositCompletedEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.user.deposit.application.DepositService;
import com.dev_high.user.deposit.application.dto.DepositDto;
import com.dev_high.common.type.DepositType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Component
@Lazy(false)
@RequiredArgsConstructor
public class DepositEventListener {
    private final DepositService depositService;
    private final ObjectMapper objectMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    @KafkaListener(topics = KafkaTopics.USER_DEPOSIT_CREATED_REQUESTED)
    public void handleUserDepositCreate(KafkaEventEnvelope<String> envelope, ConsumerRecord<?, ?> record) {
        String userId = envelope.payload();

        if (userId == null) {
            log.warn("수신된 메시지의 UserId가 null입니다. : {}, Topic: {}, Partition: {}, Offset: {}", envelope, record.topic(), record.partition(), record.offset());
            return;
        }

        DepositDto.CreateCommand command = new DepositDto.CreateCommand(userId);

        try {
            depositService.createDepositAccount(command);
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            log.warn("이미 생성된 예치금 계좌입니다. userId: {}", userId, e);
        } catch (Exception e) {
            log.error("예치금 계좌 생성 실패. userId: {}, Offset : {}", userId, record.offset(), e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaTopics.AUCTION_DEPOSIT_REFUND_REQUESTED)
    public void handleDepositRefund(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        Map<String, Object> payload = envelope.payload();
        List<String> userIds = objectMapper.convertValue(payload.get("userIds"), new TypeReference<>() {});
        String auctionId = (String) payload.get("auctionId");
        BigDecimal amount = Optional.ofNullable(payload.get("amount"))
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);

        if (userIds == null || userIds.isEmpty() || auctionId == null) {
            log.warn("필수 파라미터 누락 - userIds: {}, auctionId: {}, amount: {}, Offset: {}",
                    userIds, auctionId, amount, record.offset());
            return;
        }

        try {
            userIds.forEach(userId -> {
                DepositDto.UsageCommand command = new DepositDto.UsageCommand(
                        userId,
                        auctionId,
                        DepositType.REFUND,
                        amount
                );
                depositService.updateBalance(command);
                kafkaEventPublisher.publish(KafkaTopics.DEPOSIT_AUCTION_REFUND_RESPONSE, DepositCompletedEvent.of(userIds, auctionId, amount, "REFUND"));
            });
        } catch (NoSuchElementException e) {
            log.warn("예치금 잔액 정보를 찾을 수 없습니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 예치금 유형입니다.", e);
        } catch (Exception e) {
            log.error("보증금 환불 실패. userIds: {}, Offset : {}", userIds, record.offset(), e);
            throw e;
        }
    }
}
