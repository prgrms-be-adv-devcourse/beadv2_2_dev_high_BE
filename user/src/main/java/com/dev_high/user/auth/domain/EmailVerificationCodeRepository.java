package com.dev_high.user.auth.domain;

import java.util.Optional;

public interface EmailVerificationCodeRepository {
    void save(String email, String code, long ttlMinutes);
    Optional<String> findByEmail(String email);
    void deleteCode(String email);

}