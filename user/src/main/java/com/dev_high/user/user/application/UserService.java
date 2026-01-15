package com.dev_high.user.user.application;

import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.user.application.dto.*;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.UserAlreadyExistsException;
import com.dev_high.user.user.util.EmailMasker;
import lombok.RequiredArgsConstructor;
import com.dev_high.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerService sellerService;
    private final UserDomainService userDomainService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ApiResponseDto<UserResponse> create(CreateUserCommand command){
        if(userRepository.existsByEmailAndDeletedYn(command.email(), "N")) {
            throw new UserAlreadyExistsException();
        }

        User user = new User(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.nickname(),
                command.phone_number()
        );

        User saved = userRepository.save(user);
        userDomainService.assignRoleToUser(saved, "USER");

        if (saved != null) {
            try {
                publisher.publishEvent(saved.getId());
            } catch (Exception e) {
                log.error(">>{ }", e);
            }
        }

        return ApiResponseDto.success(
                "회원 가입이 정상적으로 처리되었습니다.",
                UserResponse.from(saved)
        );
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<UserResponse> getProfile() {
        User user = userDomainService.getUser();
        return ApiResponseDto.success(
                "회원 정보가 정상적으로 조회되었습니다.",
                UserResponse.from(user)
        );
    }

    @Transactional
    public ApiResponseDto<UserResponse> updateProfile(UpdateUserCommand command) {
        User user = userDomainService.getUser();
        user.updateUser(command);
        return ApiResponseDto.success(
                "회원 정보가 정상적으로 변경되었습니다.",
                UserResponse.from(user)
        );
    }

    @Transactional
    public ApiResponseDto<Void> updatePassword(UpdatePasswordCommand command) {
        User user = userDomainService.getUser();
        user.updatePassWord(passwordEncoder.encode(command.password()));
        return ApiResponseDto.success(
                "비밀번호가 정상적으로 변경되었습니다.",
                null
        );
    }

    @Transactional
    public ApiResponseDto<Void> delete() {
        User user = userDomainService.getUser();
        Set<String> roles = userDomainService.getUserRoles(user);

        if (roles.contains("SELLER")) {
            sellerService.removeSeller();
        }

        roles.stream()
                .filter(roleName -> !roleName.equals("SELLER"))
                .forEach(roleName -> userDomainService.revokeRoleFromUser(user, roleName));

        user.remove();
        return ApiResponseDto.success(
                "회원 탈퇴가 정상적으로 처리되었습니다.",
                null
        );
    }

    public ApiResponseDto<List<UserNicknameEmailResponse>> getUserNicknameAndEmail(UserNicknameEmailCommand command) {
        if (command.userIds() == null || command.userIds().isEmpty()) {
            return ApiResponseDto.success(List.of());
        }

        List<User> users = userRepository.findByUserIds(command.userIds());

        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<UserNicknameEmailResponse> result = new ArrayList<>(command.userIds().size());

        for (String userId : command.userIds()) {
            User user = userMap.get(userId);

            if (user == null || "Y".equals(user.getDeletedYn())) {
                result.add(new UserNicknameEmailResponse(userId, null, null));
                continue;
            }

            result.add(
                    new UserNicknameEmailResponse(
                            user.getId(),
                            EmailMasker.mask(user.getEmail()),
                            user.getNickname()
                    )
            );
        }

        return ApiResponseDto.success(result);
    }
}
