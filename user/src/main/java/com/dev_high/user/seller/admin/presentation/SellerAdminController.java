package com.dev_high.user.seller.admin.presentation;

import com.dev_high.common.context.UserContext;
import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.admin.presentation.dto.AdminSellerListRequest;
import com.dev_high.user.seller.admin.presentation.dto.SellerApproveRequest;
import com.dev_high.user.seller.admin.service.SellerAdminService;
import com.dev_high.user.seller.application.dto.SellerApproveResult;
import com.dev_high.user.seller.application.dto.SellerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/sellers")
public class SellerAdminController {

    private final SellerAdminService sellerAdminService;

    @GetMapping
    public ApiResponseDto<Page<SellerResponse>> getAuctionList(
            @ModelAttribute AdminSellerListRequest request,
            Pageable pageable) {
        return sellerAdminService.getAdminSellerList(request, pageable);
    }

    @PostMapping("/approve/batch")
    public ApiResponseDto<Void> approveBatch() {
        return sellerAdminService.runApproveBatch();
    }

    @PostMapping("/approve/selected")
    public ApiResponseDto<SellerApproveResult> approveSelected(
            @RequestBody SellerApproveRequest request
    ) {
        String adminId = UserContext.get().userId();
        return sellerAdminService.approveSelectedSeller(request.sellerIds(), adminId);
    }
}
