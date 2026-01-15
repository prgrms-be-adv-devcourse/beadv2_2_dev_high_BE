package com.dev_high.product.admin;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.presentation.dto.ProductRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1:/api/v1}/admin/products")
@Tag(name = "ProductAdmin", description = "Product admin API")
public class ProductAdminController {

    private final ProductAdminService productAdminService;

    @GetMapping
    public ApiResponseDto<Page<ProductInfo>> getProducts(Pageable pageable) {
        return ApiResponseDto.success(productAdminService.getProducts(pageable));
    }

    @GetMapping("/users/{sellerId}")
    public ApiResponseDto<Page<ProductInfo>> getProductsBySeller(@PathVariable String sellerId, Pageable pageable) {
        return ApiResponseDto.success(productAdminService.getProductsBySeller(sellerId, pageable));
    }

    @GetMapping("/{productId}")
    public ApiResponseDto<ProductInfo> getProduct(@PathVariable String productId) {
        return ApiResponseDto.success(productAdminService.getProduct(productId));
    }

    @PostMapping
    public ApiResponseDto<ProductInfo> createProduct(
        @RequestParam String sellerId,
        @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponseDto.success(productAdminService.createProduct(sellerId, request.toCommand()));
    }

    @PutMapping("/{productId}")
    public ApiResponseDto<ProductInfo> updateProduct(
        @PathVariable String productId,
        @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponseDto.success(productAdminService.updateProduct(productId, request.toCommand()));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponseDto<Void> deleteProduct(@PathVariable String productId) {
        productAdminService.deleteProduct(productId);
        return ApiResponseDto.success(null);
    }
}
