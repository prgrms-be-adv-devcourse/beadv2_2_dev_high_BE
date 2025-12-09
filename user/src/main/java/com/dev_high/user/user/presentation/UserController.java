package com.dev_high.user.user.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserService;
import com.dev_high.user.user.application.dto.UserInfo;
import com.dev_high.user.user.presentation.dto.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/user")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponseDto<UserInfo> create(@RequestBody UserSignUpRequest request) {
        return userService.create(request.toCommand());
    }

    @GetMapping("/profile")
    public ApiResponseDto<UserInfo> getProfile(@RequestHeader("X-User-Id") String userId){
        return userService.getProfile(userId);
    }


}
