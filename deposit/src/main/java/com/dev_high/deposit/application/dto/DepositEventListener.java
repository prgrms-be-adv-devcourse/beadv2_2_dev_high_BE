package com.dev_high.deposit.application.dto;

import com.dev_high.common.kafka.KafkaEventEnvelope;
import com.dev_high.deposit.application.DepositService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
/*
* 예치금 서비스 Kafka 리스너
* */
@Component
@RequiredArgsConstructor
public class DepositEventListener {
    private final DepositService depositService;

    /*
    * user 서비스에서 발행한 메시지
    * topics "user-deposit-create" 토픽 구독
    * */
    @KafkaListener(topics = "user-deposit-create")
    public void handleUserDepositCreate(KafkaEventEnvelope<Map<String, Object>> envelope, ConsumerRecord<?, ?> record) {
        try {
            Map<String, Object> val = envelope.payload();
            String userId = val.get("userId").toString();

            DepositCreateCommand command = new DepositCreateCommand(userId);

            depositService.createDepositAccount(command);
        } catch (Exception e) {
            throw e;
        }
    }
}
