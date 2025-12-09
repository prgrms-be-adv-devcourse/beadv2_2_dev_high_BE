package com.dev_high.user.user.application;

import com.dev_high.user.user.application.dto.CreateUserCommand;
import com.dev_high.user.user.application.dto.UserInfo;
import com.dev_high.user.user.domain.User;
import com.dev_high.user.user.domain.UserRepository;
import com.dev_high.user.user.exception.UserAlreadyExistsException;
import com.dev_high.user.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import com.dev_high.common.dto.ApiResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponseDto<UserInfo> create(CreateUserCommand command) {
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

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public ApiResponseDto<UserInfo> getProfile(String userId) {
        User user = findById(userId).orElseThrow(() -> new UserNotFoundException());
        return ApiResponseDto.success(UserInfo.from(user));

    }
}
