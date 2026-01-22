package com.dev_high.user.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByEmailAndDeletedYn(String email, String deletedYn);
    boolean existsByEmailAndDeletedYn(String email, String deletedYn);
    Optional<User> findByProviderAndProviderUserIdAndDeletedYn(OAuthProvider provider, String s, String deletedYn);
    List<User> findByUserIds(List<String> userIds);
}
