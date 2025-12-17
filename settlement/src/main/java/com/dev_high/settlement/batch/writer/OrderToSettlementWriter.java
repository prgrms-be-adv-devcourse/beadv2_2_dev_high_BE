package com.dev_high.settlement.batch.writer;

import com.dev_high.settlement.domain.Settlement;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.stereotype.Component;

@Component
public class OrderToSettlementWriter extends JpaItemWriter<Settlement> {


  public OrderToSettlementWriter(EntityManagerFactory emf) {
    this.setEntityManagerFactory(emf);
    this.setUsePersist(true); // insert 용
  }
}
