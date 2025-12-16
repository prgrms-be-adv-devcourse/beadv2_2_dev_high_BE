package com.dev_high.settlement.batch.processor;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementStatus;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementProcessor implements ItemProcessor<Settlement, Settlement> {

  private final List<Settlement> successSettlements;
  private final List<Settlement> failedSettlements;


  @Override
  public Settlement process(Settlement settlement) {

    settlement.ready();
    long finalAmount = settlement.getFinalAmount();
    String sellerId = settlement.getSellerId();
    try {

      // TODO: 외부 결제/예치금 API 호출 결과

      // 실패 TEST
      if (new Random().nextInt(10) < 1) { // 10% 확률로 예외 발생
        throw new RuntimeException("랜덤 시뮬레이션 예외 ");
      }

      settlement.updateStatus(SettlementStatus.COMPLETED);
      successSettlements.add(settlement);

    } catch (Exception e) {
      log.error("Settlement failed for id={}", settlement.getId(), e);
      settlement.setHistoryMessage(e.getMessage());
      settlement.updateStatus(SettlementStatus.FAILED);
      failedSettlements.add(settlement);
    }
    return settlement;
  }
}

