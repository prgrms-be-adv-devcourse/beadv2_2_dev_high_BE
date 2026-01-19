package com.dev_high.user.seller.admin.service;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.admin.presentation.dto.AdminSellerListRequest;
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
public class SellerAdminService {
    private final JobLauncher jobLauncher;
    private final Job sellerApproveJob;
    private final SellerService sellerService;
    private final SellerRepository sellerRepository;

    public ApiResponseDto<Void> runApproveBatch() {
        try {
            var exec = jobLauncher.run(
                    sellerApproveJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters()
            );
            return ApiResponseDto.success("Batch started. status=" + exec.getStatus(), null);
        } catch (Exception e) {
            log.warn("admin batch run failed {}", e.getMessage());
            return ApiResponseDto.fail("Batch run failed: " + e.getMessage(), null);
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
}
