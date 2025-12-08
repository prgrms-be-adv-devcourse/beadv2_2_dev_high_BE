package com.dev_high.user.user.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserService;
import com.dev_high.user.user.application.dto.UserInfo;
import com.dev_high.user.user.presentation.dto.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponseDto<UserInfo> create(@RequestBody UserSignUpRequest request) {
        return userService.create(request.toCommand());
    }


}
