package com.dev_high.user.auth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@ToString
@Getter
@NoArgsConstructor
@RedisHash("emailVerificationCode")
public class EmailVerificationCode {

    @Id
    private String email;

    private String code;

    @TimeToLive
    private Long ttl; // 초 단위 TTL

    public EmailVerificationCode(String email, String code, Long ttl) {
        this.email = email;
        this.code = code;
        this.ttl = ttl;
    }
}