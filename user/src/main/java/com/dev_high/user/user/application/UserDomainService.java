package com.dev_high.user.user.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.user.auth.application.dto.SocialProfileResponse;
import com.dev_high.user.user.application.role.UserRoleService;
import com.dev_high.user.user.domain.Role;
import com.dev_high.user.user.domain.RoleRepository;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public User getUser() {
        String userId = UserContext.get().userId();
        return userRepository.findById(userId)
                .filter(user -> !"Y".equals(user.getDeletedYn()))
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public User getUser(String userId) {
        return userRepository.findById(userId)
                .filter(user -> !"Y".equals(user.getDeletedYn()))
                .orElseThrow(UserNotFoundException::new);
    }

    public Set<String> getUserRoles(User user) {
        return userRoleService.getUserRoles(user);
    }

    public void assignRoleToUser(User user, String roleName) {
        userRoleService.assignRoleToUser(user, roleName);
    }

    public void revokeRoleFromUser(User user, String roleName) {
        userRoleService.revokeRoleFromUser(user, roleName);
    }

    public User createOAuthUser(SocialProfileResponse socialProfileResponse) {
        User user = new User (
                socialProfileResponse.email(),
                socialProfileResponse.name(),
                socialProfileResponse.nickname(),
                socialProfileResponse.phoneNumber(),
                socialProfileResponse.provider(),
                socialProfileResponse.providerUserId()

        );

        User saved = userRepository.save(user);
        assignRoleToUser(saved, "USER");

        if (saved != null) {
            try {
                publisher.publishEvent(saved.getId());
            } catch (Exception e) {
                log.error(">>{ }", e);
            }
        }

        return saved;
    }

    public UUID getRoleIdByName(String roleName) {
        Optional<Role> role = roleRepository.findByName(roleName);
        return role.map(Role::getId).orElse(null);
    }
}
