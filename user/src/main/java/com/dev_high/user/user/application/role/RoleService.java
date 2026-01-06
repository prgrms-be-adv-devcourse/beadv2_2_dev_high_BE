package com.dev_high.user.user.application.role;

import com.dev_high.user.user.domain.Role;
import com.dev_high.user.user.domain.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role findRole(String roleName) {
        return roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("역할이 존재하지 않습니다."));
    }
}
