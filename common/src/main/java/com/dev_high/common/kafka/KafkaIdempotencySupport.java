package com.dev_high.common.kafka;

import com.dev_high.common.kafka.domain.ConsumedEvent;
import com.dev_high.common.kafka.domain.ConsumedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaIdempotencySupport {

    private final ConsumedEventJpaRepository consumedEventJpaRepository;

    public boolean alreadyConsumed(UUID eventId, String moduleName) {
        try {
            consumedEventJpaRepository.save(new ConsumedEvent(eventId, moduleName));
            return false;
        } catch (DataIntegrityViolationException e) {
            return true;
        }
    }
}
