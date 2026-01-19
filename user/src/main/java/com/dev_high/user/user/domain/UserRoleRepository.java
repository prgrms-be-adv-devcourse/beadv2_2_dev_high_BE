package com.dev_high.user.user.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRoleRepository {
    Set<String> findRoleNamesByUserId(String userId);
    void save(UserRole userRole);
    Optional<UserRole> findByUserIdAndRoleName(String id, String roleName);
    void delete(UserRole userRole);
    List<UserRole> findByUserId(String id);
    void deleteAll(List<UserRole> userRoles);
    boolean existsByUserAndRole(User user, Role role);
}
