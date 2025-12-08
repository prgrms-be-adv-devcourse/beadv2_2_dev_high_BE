package com.dev_high.common.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${spring.application.name}")
  private String moduleName;

  /**
   * 지정한 토픽으로 이벤트 발행
   *
   * @param topic   KafkaTopics 상수
   * @param payload 발행할 이벤트 DTO
   * @param <T>     이벤트 타입
   */
  public <T> void publish(String topic, T payload) {
    var envelope = KafkaEventEnvelope.wrap(moduleName, payload);
    long startTime = System.currentTimeMillis();

    kafkaTemplate.send(topic, envelope).whenComplete((result, ex) -> {
      long latency = System.currentTimeMillis() - startTime;

      if (ex != null) {
        log.error("[Kafka-Publish-Error] topic={}, module={}, eventType={}, latency={}ms", topic,
            moduleName, payload.getClass().getSimpleName(), latency, ex);
      } else {
        var metadata = result.getRecordMetadata();

        log.info(
            "[Kafka-Publish] topic={}, partition={}, offset={}, sendModule={}, eventType={}, timestamp={}, latency={}ms",
            topic,
            metadata.partition(),
            metadata.offset(),
            moduleName,
            payload.getClass().getSimpleName(),
            envelope.timestamp(), // 발행 시각을 envelope 기준으로
            latency
        );

      }
    });

  }

}


