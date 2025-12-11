package com.dev_high.settlement.config.kafka;

import com.dev_high.settlement.config.dto.SettlementConfirmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaIssueProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info(
                                "정산 확정 이벤트 발행 성공: key={}, offset={}",
                                key,
                                result.getRecordMetadata().offset()
                        );
                    } else {
                        log.error(
                                "정산 확정 이벤트 발행 실패: key={}",
                                key,
                                ex
                        );
                    }
                });
    }
}
