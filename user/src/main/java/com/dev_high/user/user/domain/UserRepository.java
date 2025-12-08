package com.dev_high.user.user.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);
    Optional<User> findById(String id);
    boolean existsByEmail(String email);
}
