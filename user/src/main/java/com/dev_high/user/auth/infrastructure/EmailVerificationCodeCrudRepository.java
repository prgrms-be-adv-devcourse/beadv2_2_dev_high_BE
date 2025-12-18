package com.dev_high.user.auth.infrastructure;

import com.dev_high.user.auth.domain.EmailVerificationCode;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationCodeCrudRepository extends CrudRepository<EmailVerificationCode, String>{
}
