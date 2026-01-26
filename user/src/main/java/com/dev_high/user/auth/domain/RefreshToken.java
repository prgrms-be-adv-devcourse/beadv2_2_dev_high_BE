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
    private String token;

    private String userId;

    @TimeToLive
    private Long ttl; // 초 단위 TTL

    public RefreshToken(String token, String userId, Long ttl) {
        this.token = token;
        this.userId = userId;
        this.ttl = ttl;
    }
}
