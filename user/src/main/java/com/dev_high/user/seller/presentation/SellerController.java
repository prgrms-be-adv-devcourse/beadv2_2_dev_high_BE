package com.dev_high.user.seller.presentation;


import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.seller.application.dto.SellerInfo;
import com.dev_high.user.seller.presentation.dto.SellerSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/users/sellers")
@RestController
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping
    public ApiResponseDto<SellerInfo> create(@RequestBody SellerSignUpRequest request) {
        return sellerService.create(request.toCommand());
    }

}
