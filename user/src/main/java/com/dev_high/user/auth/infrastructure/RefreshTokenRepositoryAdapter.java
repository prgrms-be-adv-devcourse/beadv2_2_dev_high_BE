package com.dev_high.user.auth.infrastructure;

import com.dev_high.user.auth.domain.RefreshToken;
import com.dev_high.user.auth.domain.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenCrudRepository refreshTokenCrudRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenCrudRepository refreshTokenCrudRepository) {
        this.refreshTokenCrudRepository = refreshTokenCrudRepository;
    }

    @Override
    public void save(String userId, String refreshToken, long ttlMilliseconds) {
        refreshTokenCrudRepository.save(new RefreshToken(userId, refreshToken, ttlMilliseconds/1000));
    }

    @Override
    public Optional<RefreshToken> findByUserId(String userId) {
        return refreshTokenCrudRepository.findById(userId);
    }

    @Override
    public void deleteById(String userId) {
        refreshTokenCrudRepository.deleteById(userId);
    }
}
