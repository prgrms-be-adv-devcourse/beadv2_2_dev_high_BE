package com.dev_high.user.user.application;

import com.dev_high.common.context.UserContext;
import com.dev_high.user.auth.application.AuthService;
import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.seller.domain.SellerStatus;
import com.dev_high.user.user.application.dto.CreateUserCommand;
import com.dev_high.user.user.application.dto.UpdatePasswordCommand;
import com.dev_high.user.user.application.dto.UpdateUserCommand;
import com.dev_high.user.user.application.dto.UserInfo;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.domain.UserRole;
import com.dev_high.user.user.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import com.dev_high.common.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerService sellerService;
    private final AuthService authService;
    private final UserDomainService userDomainService;

    @Transactional
    public ApiResponseDto<UserInfo> create(CreateUserCommand command){
        if(userRepository.existsByEmail(command.email())) {
         throw new UserAlreadyExistsException();
        }
        User user = new User(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.nickname(),
                command.phone_number(),
                command.zip_code(),
                command.state(),
                command.city(),
                command.detail()
        );
        User saved = userRepository.save(user);
        return ApiResponseDto.success(UserInfo.from(saved));
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<UserInfo> getProfile() {
        User user = userDomainService.getUser();
        return ApiResponseDto.success(UserInfo.from(user));

    }

    @Transactional
    public ApiResponseDto<UserInfo> updateProfile(UpdateUserCommand command) {
        User user = userDomainService.getUser();
        user.updateUser(command);
        return ApiResponseDto.success(UserInfo.from(user));
    }

    @Transactional
    public ApiResponseDto<Void> updatePassword(UpdatePasswordCommand command) {
        User user = userDomainService.getUser();
        user.updatePassWord(passwordEncoder.encode(command.password()));
        return ApiResponseDto.success(null);
    }

    @Transactional
    public ApiResponseDto<Void> delete() {
        User user = userDomainService.getUser();
        if(user.getUserRole() == UserRole.SELLER) {
            sellerService.deleteSeller(SellerStatus.WITHDRAWN);
        }
        user.deleteUser();
        return ApiResponseDto.success(null);
    }


    @Transactional
    public ApiResponseDto<Void> logout() {
        String userId = UserContext.get().userId();
        authService.logout(userId);
        return ApiResponseDto.success(null);
    }
}
