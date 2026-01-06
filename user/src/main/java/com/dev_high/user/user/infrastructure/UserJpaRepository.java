package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.OAuthProvider;
import com.dev_high.user.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, String> {

    boolean existsByEmailAndDeletedYn(String email, String deletedYn);
    Optional<User> findByEmailAndDeletedYn(String email, String deletedYn);
    Optional<User> findByProviderAndProviderUserIdAndDeletedYn(
            OAuthProvider provider,
            String userId,
            String deletedYn
    );
}
