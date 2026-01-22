package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.Role;
import com.dev_high.user.user.domain.RoleRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name);
    }
}
