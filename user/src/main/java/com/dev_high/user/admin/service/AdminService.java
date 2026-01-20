package com.dev_high.user.admin.service;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.admin.service.dto.UserDetailResponse;
import com.dev_high.user.admin.service.dto.UserFilterCondition;
import com.dev_high.user.admin.domain.AdminRepository;
import com.dev_high.user.admin.presentation.dto.AdminSellerListRequest;
import com.dev_high.user.admin.presentation.dto.AdminUserListRequest;
import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.seller.application.dto.SellerApproveResult;
import com.dev_high.user.seller.application.dto.SellerResponse;
import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final JobLauncher jobLauncher;
    private final Job sellerApproveJob;
    private final SellerService sellerService;
    private final SellerRepository sellerRepository;
    private  final AdminRepository adminRepository;

    public ApiResponseDto<Void> runApproveBatch() {
        try {
            var exec = jobLauncher.run(
                    sellerApproveJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters()
            );
            return ApiResponseDto.success("배치 작업이 정상적으로 시작되었습니다. 상태: "+ exec.getStatus(), null);
        } catch (Exception e) {
            log.warn("관리자 승인 배치 실행 중 오류가 발생했습니다: {}", e.getMessage());
            return ApiResponseDto.fail("배치 실행에 실패했습니다. 오류 메시지: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponseDto<SellerApproveResult> approveSelectedSeller(List<String> sellerIds, String approvedBy) {
        List<Seller> sellers = sellerRepository.findByIdIn(sellerIds);
        return ApiResponseDto.success(sellerService.approveSellers(sellers, approvedBy));
    }

    public ApiResponseDto<Page<SellerResponse>> getAdminSellerList(AdminSellerListRequest request, Pageable pageable) {
        Page<SellerResponse> result = sellerService.getAdminSellerList(request, pageable);
        return ApiResponseDto.success("판매자 목록 조회가 완료되었습니다.", result);
    }

    /*TODO: filter add */
    public Page<UserDetailResponse> getUserList(AdminUserListRequest request, Pageable pageable){

        return adminRepository.findAll(UserFilterCondition.fromAdminRequest(request,pageable));

    }
    /*TODO */
    public ApiResponseDto<?> modifyUserStatus() {

        return null;
    }

    /*TODO */
    public ApiResponseDto<?> removeUser() {

        return null;
    }

    public Long getTodaySignUpCount() {

        return adminRepository.getTodaySignUpCount();
    }
}
