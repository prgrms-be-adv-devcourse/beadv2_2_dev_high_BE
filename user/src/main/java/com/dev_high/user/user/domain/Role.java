package com.dev_high.user.user.domain;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "role", schema = "\"user\"")
@Getter
public class Role {
        @Id
        private UUID id;

        @Column(name = "name", length = 10, nullable = false)
        private String name;

}