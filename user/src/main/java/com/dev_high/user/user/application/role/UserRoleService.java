package com.dev_high.user.user.application.role;

import com.dev_high.user.user.domain.Role;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRole;
import com.dev_high.user.user.domain.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;

    @Transactional
    public void assignRoleToUser(User user, String roleName) {
        Role role = roleService.findRole(roleName);
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
    }

    @Transactional(readOnly = true)
    public Set<String> getUserRoles(User user) {
        return userRoleRepository.findRoleNamesByUserId(user.getId());
    }

    @Transactional
    public void revokeRoleFromUser(User user, String roleName) {
        Optional<UserRole> userRole = userRoleRepository.findByUserIdAndRoleName(user.getId(), roleName);
        userRole.ifPresent(userRoleRepository::delete);
    }

    @Transactional
    public void revokeRolesFromUser(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        userRoleRepository.deleteAll(userRoles);
    }
}
