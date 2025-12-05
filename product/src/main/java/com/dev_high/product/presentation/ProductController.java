package com.dev_high.product.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.presentation.dto.ProductRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1:/api/v1}/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<ProductInfo> createProduct(@RequestBody ProductRequest request) {
        ProductInfo productInfo = productService.registerProduct(request.toCommand());
        return ApiResponseDto.success("상품이 등록되었습니다.", productInfo);
    }
}
