package com.dev_high.product.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.presentation.dto.ProductRequest;
import com.dev_high.product.presentation.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("${api.v1:/api/v1}/products")
@Tag(name = "Product", description = "상품 관리 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록", description = "상품과 카테고리 정보를 등록합니다.")
    @PostMapping
    public ApiResponseDto<ProductInfo> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductInfo productInfo = productService.registerProduct(request.toCommand());
        return ApiResponseDto.success("상품이 등록되었습니다.", productInfo);
    }

    @Operation(summary = "상품 목록 조회", description = "페이지네이션으로 상품 목록을 조회합니다.")
    @GetMapping
    public ApiResponseDto<Page<ProductInfo>> getProducts(Pageable pageable) {
        return ApiResponseDto.success(productService.getProducts(pageable));
    }

    @Operation(summary = "상품 단건 조회", description = "카테고리 정보를 제외한 상품 단건을 조회합니다.")
    @GetMapping("/{productId}")
    public ApiResponseDto<ProductInfo> getProduct(@Parameter(description = "상품 ID", required = true) @PathVariable String productId) {
        return ApiResponseDto.success(productService.getProduct(productId));
    }

    @Operation(summary = "상품 단건 조회(카테고리 포함)", description = "카테고리 정보를 포함하여 상품을 조회합니다.")
    @GetMapping("/{productId}/categories")
    public ApiResponseDto<ProductInfo> getProductWithCategories(@Parameter(description = "상품 ID", required = true) @PathVariable String productId) {
        return ApiResponseDto.success(productService.getProductWithCategories(productId));
    }

    @Operation(summary = "상품 수정", description = "판매자 본인이고 READY 상태일 때 상품 정보를 수정합니다.")
    @PutMapping("/{productId}")
    public ApiResponseDto<ProductInfo> updateProduct(@Parameter(description = "상품 ID", required = true) @PathVariable String productId,
                                                     @Valid @RequestBody ProductUpdateRequest request) {
        return ApiResponseDto.success(productService.updateProduct(productId, request.toCommand()));
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponseDto<Void> deleteProduct(@Parameter(description = "상품 ID", required = true) @PathVariable String productId,
                                              @Parameter(description = "판매자 ID", required = true) @RequestParam String sellerId) {
        productService.deleteProduct(productId, sellerId);
        return ApiResponseDto.success(null);
    }
}
