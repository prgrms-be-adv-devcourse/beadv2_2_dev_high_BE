package com.dev_high.deposit.payment.infrastructure.kafka;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.type.DepositOrderStatus;
import com.dev_high.deposit.order.application.DepositOrderService;
import com.dev_high.deposit.order.application.dto.DepositOrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@Lazy(false)
@RequiredArgsConstructor
public class PaymentEventListener {
    private final DepositOrderService depositOrderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.DEPOSIT_PAYMENT_COMPLETE_RESPONSE)
    @Transactional
    public void handleDepositPaymentComplete(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        Map<String, Object> payload = envelope.payload();
        String id = payload.get("id").toString();

        if (id == null || id.isBlank()) {
            log.warn("필수 파라미터 누락 - userId: {}, Offset: {}", id, record.offset());
            return;
        }

        DepositOrderDto.ChangeOrderStatusCommand command = DepositOrderDto.ChangeOrderStatusCommand.of(id, DepositOrderStatus.COMPLETED);
        try {
            depositOrderService.ChangeOrderStatus(command);
        } catch (Exception e) {
            log.error("예치금 주문 완료 처리 실패 id: {}", id, e );
        }
    }

    @KafkaListener(topics = KafkaTopics.DEPOSIT_PAYMENT_FAIL_RESPONSE)
    @Transactional
    public void handleDepositPaymentFail(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        Map<String, Object> payload = envelope.payload();
        String id = payload.get("id").toString();

        if (id == null || id.isBlank()) {
            log.warn("필수 파라미터 누락 - userId: {}, Offset: {}", id, record.offset());
            return;
        }

        DepositOrderDto.ChangeOrderStatusCommand command = DepositOrderDto.ChangeOrderStatusCommand.of(id, DepositOrderStatus.FAILED);
        try {
            depositOrderService.ChangeOrderStatus(command);
        } catch (Exception e) {
            log.error("예치금 주문 완료 처리 실패 id: {}", id, e );
        }
    }
}
