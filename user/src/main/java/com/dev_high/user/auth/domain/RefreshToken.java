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
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String userId;

    private String token;

    @TimeToLive
    private Long ttl; // 초 단위 TTL

    public RefreshToken(String userId, String token, Long ttl) {
        this.userId = userId;
        this.token = token;
        this.ttl = ttl;
    }
}
