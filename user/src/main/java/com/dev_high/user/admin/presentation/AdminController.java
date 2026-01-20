package com.dev_high.user.admin.presentation;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.admin.service.AdminService;
import com.dev_high.user.admin.service.dto.UserDetailResponse;
import com.dev_high.user.admin.presentation.dto.AdminSellerListRequest;
import com.dev_high.user.admin.presentation.dto.AdminUserListRequest;
import com.dev_high.user.admin.presentation.dto.SellerApproveRequest;
import com.dev_high.user.seller.application.dto.SellerApproveResult;
import com.dev_high.user.seller.application.dto.SellerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/sellers")
    public ApiResponseDto<Page<SellerResponse>> getSellerList(
            @ModelAttribute AdminSellerListRequest request,
            Pageable pageable) {
        return adminService.getAdminSellerList(request, pageable);
    }

    @PostMapping("/sellers/approve/batch")
    public ApiResponseDto<Void> approveBatch() {
        return adminService.runApproveBatch();
    }

    @PostMapping("/sellers/approve/selected")
    public ApiResponseDto<SellerApproveResult> approveSelected(
            @RequestBody SellerApproveRequest request
    ) {
        String adminId = UserContext.get().userId();
        return adminService.approveSelectedSeller(request.sellerIds(), adminId);
    }

    @GetMapping("/users")
    public ApiResponseDto<Page<UserDetailResponse>> getUserList(@ModelAttribute AdminUserListRequest request, Pageable pageable) {

        return ApiResponseDto.success(adminService.getUserList(request,pageable));
    }
    @GetMapping("/users/count/today-signup")
    public ApiResponseDto<Long> getTodaySignUpCount(){

        return ApiResponseDto.success(adminService.getTodaySignUpCount());
    }

    @PatchMapping("/users")
    public ApiResponseDto<ApiResponseDto<?>> modifyUserStatus(){

        return ApiResponseDto.success(adminService.modifyUserStatus());
    }

    @DeleteMapping("/users")
    public ApiResponseDto<ApiResponseDto<?>> removeUser(){

        return ApiResponseDto.success(adminService.removeUser());
    }
}
