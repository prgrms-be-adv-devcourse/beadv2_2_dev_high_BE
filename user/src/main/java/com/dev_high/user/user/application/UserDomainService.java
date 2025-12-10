package com.dev_high.user.user.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.domain.UserRole;
import com.dev_high.user.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser() {
        String userId = UserContext.get().userId();
        return userRepository.findById(userId)
                .filter(user -> !"Y".equals(user.getDeletedYn())) // 삭제 여부 필터
                .orElseThrow(UserNotFoundException::new);
    }

    public void updateUserRole(User user, UserRole role) {
        user.updateRole(role);
    }
}
