package com.dev_high.user.auth.domain;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String userId, String refreshToken, long ttlMilliseconds);
    Optional<RefreshToken> findById(String refreshToken);
    void deleteById(String refreshToken);
}