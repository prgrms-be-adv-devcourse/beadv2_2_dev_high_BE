package com.dev_high.product.admin;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.application.dto.DashboardCategoryCountItem;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1:/api/v1}/admin/products")
@Tag(name = "ProductAdmin", description = "Product admin API")
public class ProductAdminController {

    private final ProductAdminService productAdminService;

    @PostMapping
    public ApiResponseDto<ProductInfo> createProduct(
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponseDto.success(productAdminService.createProduct(request.toCommand()));
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

    // 상품 동적 다건 조회
    @GetMapping
    public ApiResponseDto<Page<ProductInfo>> getProducts(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) String sellerId,
        Pageable pageable
    ) {
        return ApiResponseDto.success(productAdminService.searchProducts(name, description, sellerId, pageable));
    }

    // 상품 단건 조회
    @GetMapping("/{productId}")
    public ApiResponseDto<ProductInfo> getProduct(@PathVariable String productId) {
        return ApiResponseDto.success(productAdminService.getProduct(productId));
    }

    @GetMapping("/dashboard/charts/category-product-count")
    public ApiResponseDto<List<DashboardCategoryCountItem>> getCategoryProductCount(
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String timezone
    ) {
        return ApiResponseDto.success(productAdminService.getCategoryProductCounts(from, to, limit, timezone));
    }


}
