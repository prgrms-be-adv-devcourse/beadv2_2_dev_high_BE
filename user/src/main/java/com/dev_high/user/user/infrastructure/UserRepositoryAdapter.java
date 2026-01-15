package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.OAuthProvider;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmailAndDeletedYn(String email, String deletedYn) {
        return userJpaRepository.findByEmailAndDeletedYn(email, deletedYn);
    }

    @Override
    public boolean existsByEmailAndDeletedYn(String email, String deletedYn)  {
        return userJpaRepository.existsByEmailAndDeletedYn(email, deletedYn);
    }

    @Override
    public Optional<User> findByProviderAndProviderUserIdAndDeletedYn(OAuthProvider provider, String userId, String deletedYn) {
        return userJpaRepository.findByProviderAndProviderUserIdAndDeletedYn(provider, userId, deletedYn);
    }

    @Override
    public List<User> findByUserIds(List<String> userIds) {
        return userJpaRepository.findByIdIn(userIds);
    }
}
