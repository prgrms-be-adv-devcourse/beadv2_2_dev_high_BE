package com.dev_high.user.user.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.user.application.UserService;
import com.dev_high.user.user.application.dto.UserAddressResponse;
import com.dev_high.user.user.application.dto.UserNicknameEmailResponse;
import com.dev_high.user.user.application.dto.UserResponse;
import com.dev_high.user.user.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("")
    public ApiResponseDto<List<UserNicknameEmailResponse>> getUserNicknameAndEmail(@RequestBody UserNicknameEmailRequest request) {
        return userService.getUserNicknameAndEmail(request.toCommand());
    }

    @GetMapping("/address")
    public ApiResponseDto<List<UserAddressResponse>> getAddressList() {
        return userService.getAddressList();
    }

    @PostMapping("/address")
    public ApiResponseDto<UserAddressResponse> registerAddress(@RequestBody UserAddressRequest request) {
        return userService.registerAddress(request.toCommand());
    }

    @PutMapping("/address/{addressId}")
    public ApiResponseDto<UserAddressResponse> updateAddress(@PathVariable String addressId, @RequestBody UserAddressRequest request) {
        return userService.updateAddress(addressId, request.toCommand());
    }

    @DeleteMapping("/address/{addressId}")
    public ApiResponseDto<Void> deleteAddress(@PathVariable String addressId) {
        return userService.deleteAddress(addressId);
    }
}
