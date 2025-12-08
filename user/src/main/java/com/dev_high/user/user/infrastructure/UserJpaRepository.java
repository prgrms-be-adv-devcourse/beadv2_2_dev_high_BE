package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);
}
