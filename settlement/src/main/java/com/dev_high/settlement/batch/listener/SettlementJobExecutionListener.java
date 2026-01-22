package com.dev_high.settlement.batch.listener;


import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.settlement.settle.domain.settle.Settlement;
import com.dev_high.settlement.settle.domain.settle.SettlementStatus;
import com.dev_high.settlement.settle.domain.history.SettlementHistory;
import com.dev_high.settlement.settle.infrastructure.SettlementHistoryJpaRepository;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementJobExecutionListener implements JobExecutionListener {

  private final SettlementHistoryJpaRepository settlementHistoryJpaRepository;
  private final KafkaEventPublisher publisher;
  private final RestTemplate restTemplate;
  private final List<Settlement> successSettlements;
  private final List<Settlement> failedSettlements;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    // 필요시 beforeJob 처리
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    // 정산 결과 알림 및 이력 저장 처리
    Map<String, Long> sellerAmountMap = successSettlements.stream()
        .filter(s -> s.getStatus() == SettlementStatus.COMPLETED)
        .collect(Collectors.groupingBy(
            Settlement::getSellerId,
            Collectors.summingLong(Settlement::getFinalAmount)
        ));

    sellerAmountMap.forEach((sellerId, totalAmount) -> {
      boolean depositSuccess = requestDeposit(sellerId, totalAmount);
      persistSettlementSummary(sellerId, totalAmount, depositSuccess);

      if (depositSuccess) {
        String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount);
        publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
            new NotificationRequestEvent(
                    List.of(sellerId),
                    formattedAmount + "원 정산이 완료되었습니다.",
                    "/mypage",
                    "SETTLEMENT_SUCCESS",
                    ""
            )
        );
      }
    });

    failedSettlements.forEach(settlement -> {

      log.error("Settlement ID={} 정산 실패, 시도 횟수={}", settlement.getId(), settlement.getTryCnt());

      if (settlement.getTryCnt() > 3) {
        publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
            new NotificationRequestEvent(
                    List.of("SYSTEM"), // 어드민 아이디 example
                    String.format("Settlement ID %s - 정산 실패 횟수: %d", settlement.getId(), settlement.getTryCnt()),
                    "/settlement/" + settlement.getId(),
                    "SETTLEMENT_FAILED",
                    ""
            )
        );
      }
    });
    // 이력 생성용 + id로 오름차순 정렬
    List<SettlementHistory> histories = Stream.concat(successSettlements.stream(),
            failedSettlements.stream())
        .sorted(Comparator.comparing(Settlement::getId))
        .map(SettlementHistory::fromSettlement)
        .toList();

    final int batchSize = 50;
    for (int i = 0; i < histories.size(); i += batchSize) {
      int end = Math.min(i + batchSize, histories.size());
      settlementHistoryJpaRepository.saveAll(histories.subList(i, end));
      settlementHistoryJpaRepository.flush(); // DB 반영
    }

    failedSettlements.clear();
    successSettlements.clear();
  }

  private boolean requestDeposit(String sellerId, Long totalAmount) {
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("userId", sellerId);
      map.put("type", "CHARGE");
      map.put("depositOrderId", "SETTLEMENT_SUMMARY");
      map.put("amount", totalAmount);

      HttpEntity<Map<String, Object>> entity = HttpUtil.createDirectEntity(map);
      String url = "http://DEPOSIT-SERVICE/api/v1/deposit/usages";

      ResponseEntity<ApiResponseDto<?>> response = restTemplate.exchange(
          url,
          HttpMethod.POST,
          entity,
          new ParameterizedTypeReference<>() {
          }
      );

      if (response.getBody() == null) {
        log.error("Deposit API response is null for sellerId={}", sellerId);
        return false;
      }
      log.info("deposit success sellerId={}", sellerId);
      return true;
    } catch (Exception e) {
      log.error("deposit request failed sellerId={}", sellerId, e);
      return false;
    }
  }

  private void persistSettlementSummary(String sellerId, Long totalAmount, boolean success) {
    // TODO: summary 테이블에 합산 정산 결과 저장 (성공/실패 포함)
  }
}
