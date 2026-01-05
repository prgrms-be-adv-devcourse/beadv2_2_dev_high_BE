package com.dev_high.settlement.batch.writer;

import com.dev_high.settlement.domain.settle.Settlement;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.stereotype.Component;

@Component
public class SettlementWriter extends JpaItemWriter<Settlement> {

  public SettlementWriter(EntityManagerFactory emf) {
    // 기존 정산 엔티티를 merge로 업데이트
    this.setEntityManagerFactory(emf);
    this.setUsePersist(false); // merge 사용
  }
}
