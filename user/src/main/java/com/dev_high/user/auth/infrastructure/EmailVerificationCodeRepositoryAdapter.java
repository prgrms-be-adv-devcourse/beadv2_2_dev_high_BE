package com.dev_high.user.auth.infrastructure;

import com.dev_high.user.auth.domain.EmailVerificationCode;
import com.dev_high.user.auth.domain.EmailVerificationCodeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EmailVerificationCodeRepositoryAdapter implements EmailVerificationCodeRepository {

    private final EmailVerificationCodeCrudRepository emailVerificationCodeCrudRepository;

    public EmailVerificationCodeRepositoryAdapter(EmailVerificationCodeCrudRepository emailVerificationCodeCrudRepository) {
        this.emailVerificationCodeCrudRepository = emailVerificationCodeCrudRepository;
    }

    @Override
    public void save(String email, String code, long ttlMinutes) {
        emailVerificationCodeCrudRepository.save(new EmailVerificationCode(email, code, ttlMinutes * 60));
    }

    @Override
    public Optional<String> findByEmail(String email) {
        return emailVerificationCodeCrudRepository.findById(email)
                .map(EmailVerificationCode::getCode);
    }

    @Override
    public void deleteCode(String email) {
        emailVerificationCodeCrudRepository.deleteById(email);
    }
}
