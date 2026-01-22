package com.dev_high.common.kafka.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumed_event",schema = "public")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsumedEvent {

    @EmbeddedId
    private ConsumedEventId id;

    @Column(name = "consumed_at", nullable = false)
    private OffsetDateTime consumedAt;


    public ConsumedEvent(UUID eventId, String moduleName) {
        this.id = new ConsumedEventId(eventId, moduleName);
        this.consumedAt = OffsetDateTime.now();
    }
}
