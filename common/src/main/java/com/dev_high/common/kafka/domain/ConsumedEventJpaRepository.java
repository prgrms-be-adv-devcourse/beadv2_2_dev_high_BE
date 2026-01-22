package com.dev_high.common.kafka.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedEventJpaRepository extends JpaRepository<ConsumedEvent, ConsumedEventId> {
}
