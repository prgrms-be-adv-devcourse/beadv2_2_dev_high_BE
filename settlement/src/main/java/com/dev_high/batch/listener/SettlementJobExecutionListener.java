package com.dev_high.batch.listener;


import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.common.kafka.event.NotificationRequestEvent;
import com.dev_high.common.kafka.topics.KafkaTopics;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.type.DepositType;
import com.dev_high.common.type.NotificationCategory;
import com.dev_high.common.util.HttpUtil;
import com.dev_high.settle.domain.group.SettlementGroupRepository;
import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementStatus;
import com.dev_high.settle.domain.history.SettlementHistory;
import com.dev_high.settle.infrastructure.SettlementHistoryJpaRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
  private final SettlementGroupRepository settlementGroupRepository;
  private final KafkaEventPublisher publisher;
  private final RestTemplate restTemplate;
  private final List<Settlement> allSettlements;
  private final List<Settlement> failedSettlements;


  @Override
  public void beforeJob(JobExecution jobExecution) {
    // 필요시 beforeJob 처리
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    // 정산 결과 알림 및 이력 저장 처리


    Map<String, SettleTotal> groupedTotals = allSettlements.stream()
        .filter(settlement -> settlement.getStatus() == SettlementStatus.COMPLETED)
        .filter(settlement -> settlement.getSettlementGroupId() != null)
        .collect(Collectors.groupingBy(Settlement::getSettlementGroupId,
            Collectors.collectingAndThen(Collectors.toList(), settlements -> {
              String sellerId = settlements.get(0).getSellerId();
              BigDecimal totalFinalAmount = settlements.stream()
                  .map(Settlement::getFinalAmount)
                  .map(this::safeAmount)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
              BigDecimal totalCharge = settlements.stream()
                  .map(Settlement::getCharge)
                  .map(this::safeAmount)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
              return new SettleTotal(settlements.get(0).getSettlementGroupId(), sellerId,
                  totalCharge, totalFinalAmount);
            })));

    for (SettleTotal total : groupedTotals.values()) {
      String groupId = total.groupId();
      String sellerId = total.sellerId();
      BigDecimal totalFinalAmount = total.totalFinalAmount();
      BigDecimal totalCharge = total.totalCharge();

      var groupOpt = settlementGroupRepository.findById(groupId);
      if (groupOpt.isEmpty()) {
        continue;
      }
      var group = groupOpt.get();
      if (totalFinalAmount.compareTo(BigDecimal.ZERO) > 0) {
        boolean depositSuccess = requestDeposit(sellerId, totalFinalAmount);
        if (depositSuccess) {
          group.addPaid(totalCharge, totalFinalAmount);
          String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA)
              .format(totalFinalAmount);
          publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
              new NotificationRequestEvent(
                  List.of(sellerId),
                  formattedAmount + "원 정산이 완료되었습니다.",
                  "/mypage?tab=6",
                  NotificationCategory.Type.SETTLEMENT_SUCCESS
              )
          );
        }
      }
      group.refreshDepositStatus();
      settlementGroupRepository.save(group);
    }

    failedSettlements.forEach(settlement -> {
      log.error("Settlement ID={} 정산 실패, 시도 횟수={}", settlement.getId(), settlement.getTryCnt());
      if (settlement.getTryCnt() > 3) {
        publisher.publish(KafkaTopics.NOTIFICATION_REQUEST,
            new NotificationRequestEvent(
                List.of("SYSTEM"), // 어드민 아이디 example
                String.format("Settlement ID %s - 정산 실패 횟수: %d", settlement.getId(), settlement.getTryCnt()),
                "/settlement/" + settlement.getId(),
                NotificationCategory.Type.SETTLEMENT_FAILED
            )
        );
      }
    });
    // 이력 생성용 + id로 오름차순 정렬
    List<SettlementHistory> histories = allSettlements.stream()
        .map(SettlementHistory::fromSettlement)
        .toList();

    final int batchSize = 50;
    for (int i = 0; i < histories.size(); i += batchSize) {
      int end = Math.min(i + batchSize, histories.size());
      settlementHistoryJpaRepository.saveAll(histories.subList(i, end));
      settlementHistoryJpaRepository.flush(); // DB 반영
    }
    failedSettlements.clear();
    allSettlements.clear();
  }

  private boolean requestDeposit(String sellerId, BigDecimal totalAmount) {
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("userId", sellerId);
      map.put("type", DepositType.CHARGE);
      map.put("amount", totalAmount);

      HttpEntity<Map<String, Object>> entity = HttpUtil.createDirectEntity(map);
      String url = "http://USER-SERVICE/api/v1/deposit/usages";

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

  private BigDecimal safeAmount(BigDecimal amount) {
    if (amount == null) {
      return BigDecimal.ZERO;
    }
    return amount.setScale(0, java.math.RoundingMode.DOWN);
  }

}
