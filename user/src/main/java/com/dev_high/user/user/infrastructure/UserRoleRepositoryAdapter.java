package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.UserRole;
import com.dev_high.user.user.domain.UserRoleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRoleRepositoryAdapter implements UserRoleRepository {
    private final UserRoleJpaRepository userRoleJpaRepository;

    public UserRoleRepositoryAdapter(UserRoleJpaRepository userRoleJpaRepository) {
        this.userRoleJpaRepository = userRoleJpaRepository;
    }

    @Override
    public Set<String> findRoleNamesByUserId(String userId) {
        return userRoleJpaRepository.findRoleNamesByUserId(userId);
    }

    @Override
    public void save(UserRole userRole) {
        userRoleJpaRepository.save(userRole);
    }

    @Override
    public Optional<UserRole> findByUserIdAndRoleName(String userId, String roleName) {
        return userRoleJpaRepository.findByUserIdAndRoleName(userId, roleName);
    }

    @Override
    public void delete(UserRole userRole) {
        userRoleJpaRepository.delete(userRole);
    }

    @Override
    public List<UserRole> findByUserId(String userId) {
        return userRoleJpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteAll(List<UserRole> userRoles) {
        userRoleJpaRepository.deleteAll(userRoles);
    }
}
