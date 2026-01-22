package com.dev_high.user.user.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserService;
import com.dev_high.user.user.application.dto.UserResponse;
import com.dev_high.user.user.presentation.dto.PasswordUpdateRequest;
import com.dev_high.user.user.presentation.dto.UserSignUpRequest;
import com.dev_high.user.user.presentation.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ApiResponseDto<UserResponse> create(@RequestBody UserSignUpRequest request) {
        return userService.create(request.toCommand());
    }

    @GetMapping("/profile")
    public ApiResponseDto<UserResponse> getProfile(){
        return userService.getProfile();
    }

    @PutMapping("/profile")
    public ApiResponseDto<UserResponse> updateProfile(@RequestBody UserUpdateRequest request) {
        return userService.updateProfile(request.toCommand());
    }

    @PatchMapping("/password")
    public ApiResponseDto<Void> updatePassword(@RequestBody PasswordUpdateRequest request) {
        return userService.updatePassword(request.toCommand());
    }

    @DeleteMapping
    public ApiResponseDto<Void> deleteUser(){
        return userService.delete();
    }

    }
