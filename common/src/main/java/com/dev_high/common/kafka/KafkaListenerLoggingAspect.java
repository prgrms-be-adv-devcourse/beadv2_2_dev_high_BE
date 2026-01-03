package com.dev_high.common.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class KafkaListenerLoggingAspect {

  private final ObjectMapper objectMapper;
  private  final KafkaIdempotencySupport kafkaIdempotencySupport;
  private static final Logger log = LoggerFactory.getLogger("KAFKA_LOG");

  @Value("${spring.application.name:unknown-module}")
  private String appName;  // 수신 모듈 이름


  /**
   * KafkaEventEnvelope를 첫 번째 파라미터로 받는 모든 @KafkaListener 메서드에 적용
   */
  @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
  public Object logKafkaEvent(ProceedingJoinPoint joinPoint)
      throws Throwable {
    Object[] args = joinPoint.getArgs();

    KafkaEventEnvelope<?> envelope = null;
    ConsumerRecord<?, ?> record = null;

    for (Object arg : args) {
      if (arg instanceof KafkaEventEnvelope<?> env) {
        envelope = env;
      }
      if (arg instanceof ConsumerRecord<?, ?> rec) {
        record = rec;
      }
    }

    // envelope 없는 경우 → 로깅 스킵하고 그대로 진행
    if (envelope == null) {
      log.warn("[Kafka-Listener] Envelope missing. args={}",
          Arrays.toString(args));

      return joinPoint.proceed();
    }
    // uuid , module name으로 유니크체크
    if(kafkaIdempotencySupport.alreadyConsumed(envelope.eventId(),envelope.module())){

          return null;
      }

    // latency
    long receiveTimestamp = System.currentTimeMillis();
    long latency = receiveTimestamp - envelope.timestamp();

    if (latency < 0) {
      latency = 0;
    }

    // null-safe 기본값
    String topic = record != null ? record.topic() : "unknown";
    Object partition = record != null ? record.partition() : "unknown";
    Object offset = record != null ? record.offset() : "unknown";

    String payloadJson;
    try {
      payloadJson = objectMapper.writeValueAsString(envelope.payload());
    } catch (Exception e) {
      payloadJson = "Failed to serialize payload";
    }

    log.info(
        "[Kafka-Receive] topic={}, partition={}, offset={}, sendModule={}, receiveModule={}, type={}, receiveTimestamp={}, latency={}ms, payload={}",
        topic,
        partition,
        offset,
        envelope.module(),  // 발행 모듈
        appName,            // 수신 모듈
        envelope.eventType(),
        receiveTimestamp,   // 수신 시각
        latency,
        payloadJson
    );

    return joinPoint.proceed(); // 실제 리스너 호출, 예외 발생 시 그대로 던짐
  }
}