package com.dev_high.batch.processor;

import com.dev_high.settle.domain.settle.Settlement;
import com.dev_high.settle.domain.settle.SettlementStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementProcessor implements ItemProcessor<Settlement, Settlement> {

    private final List<Settlement> allSettlements;
    private final List<Settlement> failedSettlements;

    @Override
    public Settlement process(Settlement settlement) {

        
        try {
            // 시도 횟수 증가
            settlement.retry();
            // 임시: 90% 성공, 10% 실패
            boolean success = ThreadLocalRandom.current().nextInt(100) < 90;
            if (!success) {
                throw new RuntimeException("Random settlement failure");
            }

            // 성공 시 상태 완료 처리
            settlement.updateStatus(SettlementStatus.COMPLETED);

        } catch (Exception e) {
            log.error("Settlement failed for id={}", settlement.getId(), e);
            settlement.setHistoryMessage(e.getMessage());
            // 실패 시 재시도 대상으로 전환
            settlement.updateStatus(SettlementStatus.FAILED);
            failedSettlements.add(settlement);

        }finally {
            allSettlements.add(settlement);
        }
        return settlement;
    }
}
