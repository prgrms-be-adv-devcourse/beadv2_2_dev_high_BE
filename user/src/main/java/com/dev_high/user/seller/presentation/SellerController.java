package com.dev_high.user.seller.presentation;


import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.seller.application.dto.SellerInfo;
import com.dev_high.user.seller.presentation.dto.SellerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/sellers")
@RestController
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping
    public ApiResponseDto<SellerInfo> create(@RequestBody SellerRequest request) {
        return sellerService.create(request.toCommand());
    }

    @GetMapping
    public ApiResponseDto<SellerInfo> getProfile() {
        return sellerService.getProfile();
    }

    @PutMapping
    public ApiResponseDto<SellerInfo> updateProfile(@RequestBody SellerRequest request) {
        return sellerService.updateProfile(request.toCommand());
    }

    @DeleteMapping
    public ApiResponseDto<Void> deleteSeller() {
        return sellerService.delete();
    }

}
