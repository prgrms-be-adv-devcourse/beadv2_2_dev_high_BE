package com.dev_high.user.user.infrastructure;

import com.dev_high.user.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRoleJpaRepository extends JpaRepository<UserRole, String> {
    @Query("""
        SELECT ur.role.name
        FROM UserRole ur
        WHERE ur.user.id = :userId
    """)
    Set<String> findRoleNamesByUserId(String userId);

    @Query("""
        SELECT ur
        FROM UserRole ur
        WHERE ur.user.id = :userId
          AND ur.role.name = :roleName
    """)
    Optional<UserRole> findByUserIdAndRoleName(String userId, String roleName);

    List<UserRole> findByUserId(String userId);
}
