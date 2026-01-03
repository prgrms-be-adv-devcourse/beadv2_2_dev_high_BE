package com.dev_high.common.kafka;

import java.util.UUID;

public record KafkaEventEnvelope<T>(UUID eventId,
    String module,       // 이벤트 발행 모듈명
    String eventType,    // 이벤트 클래스명
    long timestamp,      // 발행 시각
    T payload            // 실제 이벤트 데이터
) {

  /**
   * payload와 module명을 받아서 envelope 생성
   */
  public static <T> KafkaEventEnvelope<T> wrap(String module, T payload) {
    return new KafkaEventEnvelope<>(UUID.randomUUID(),
        module,
        payload.getClass().getSimpleName(),
        System.currentTimeMillis(),
        payload
    );
  }
}
