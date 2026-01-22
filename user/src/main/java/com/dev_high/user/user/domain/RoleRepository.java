package com.dev_high.user.user.domain;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
}
