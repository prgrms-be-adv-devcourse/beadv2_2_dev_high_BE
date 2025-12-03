package com.dev_high.settlement.batch.listener;


import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementStatus;
import com.dev_high.settlement.domain.history.SettlementHistory;
import com.dev_high.settlement.infrastructure.SettlementHistoryJpaRepository;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementJobExecutionListener implements JobExecutionListener {

  private final SettlementHistoryJpaRepository settlementHistoryJpaRepository;
  private final KafkaEventPublisher publisher;
  private final List<Settlement> successSettlements;
  private final List<Settlement> failedSettlements;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    // 필요시 beforeJob 처리
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    Map<String, Long> sellerAmountMap = successSettlements.stream()
        .filter(s -> s.getStatus() == SettlementStatus.COMPLETED)
        .collect(Collectors.groupingBy(
            Settlement::getSellerId,
            Collectors.summingLong(Settlement::getFinalAmount)
        ));

    sellerAmountMap.forEach((sellerId, totalAmount) -> {
      String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount);

      publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
          new NotificationRequestEvent(
              List.of(sellerId),
              formattedAmount + "원 정산이 완료되었습니다.",
              "/mypage"
          )
      );
    });
    failedSettlements.forEach(settlement -> {

      log.error("Settlement ID={} 정산 실패, 시도 횟수={}", settlement.getId(), settlement.getTryCnt());

      if (settlement.getTryCnt() > 3) {
        publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
            new NotificationRequestEvent(
                List.of("SYSTEM"), // 어드민 아이디 example
                String.format("Settlement ID %s - 정산 실패 횟수: %d", settlement.getId(),
                    settlement.getTryCnt()),
                "/settlement/" + settlement.getId()
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
}