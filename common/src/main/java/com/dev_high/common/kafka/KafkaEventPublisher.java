package com.dev_high.common.kafka;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  /**
   * 지정한 토픽으로 이벤트 발행
   *
   * @param topic KafkaTopics 상수
   * @param event 발행할 이벤트 DTO
   * @param <T>   이벤트 타입
   */
  public <T> void publish(String topic, T event) {
    kafkaTemplate.send(topic, event);

  }
}