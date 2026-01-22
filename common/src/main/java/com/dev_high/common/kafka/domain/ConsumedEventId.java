package com.dev_high.common.kafka.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class ConsumedEventId implements Serializable {

    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName;
}
