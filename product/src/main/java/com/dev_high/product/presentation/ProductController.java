package com.dev_high.product.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.dto.client.product.WishlistProductResponse;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.ProductCreateResult;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1:/api/v1}/products")
@Tag(name = "Product", description = "상품 관리 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록", description = "상품과 카테고리 정보를 등록합니다. (경매 생성 포함)")
    @PostMapping
    public ApiResponseDto<ProductCreateResult> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductCreateResult result = productService.registerProduct(request.toCommand());
        return ApiResponseDto.success("상품이 등록되었습니다.", result);
    }

    @Operation(summary = "상품 목록 조회", description = "페이지네이션으로 상품 목록을 조회합니다.")
    @GetMapping
    public ApiResponseDto<Page<ProductCreateResult>> getProducts(Pageable pageable) {
        return ApiResponseDto.success(productService.getProducts(pageable));
    }

    @Operation(summary = "특정 판매자의 상품 목록 조회", description = "판매자 ID로 상품 목록을 조회합니다.")
    @GetMapping("/users/{sellerId}")
    public ApiResponseDto<List<ProductCreateResult>> getProductsBySeller(@Parameter(description = "판매자 ID", required = true) @PathVariable String sellerId) {
        return ApiResponseDto.success(productService.getProductsBySeller(sellerId));
    }

    @Operation(summary = "상품 단건 조회", description = "카테고리/경매/이미지 URL 정보를 포함한 상품 단건을 조회합니다.")
    @GetMapping("/{productId}")
    public ApiResponseDto<ProductCreateResult> getProduct(@Parameter(description = "상품 ID", required = true) @PathVariable String productId) {
        return ApiResponseDto.success(productService.getProduct(productId));
    }

    @Deprecated
    @Operation(summary = "상품 단건 조회(카테고리/경매 포함)", description = "카테고리와 경매 정보를 포함하여 상품을 조회합니다.")
    @GetMapping("/{productId}/categories")
    public ApiResponseDto<ProductCreateResult> getProductWithCategories(@Parameter(description = "상품 ID", required = true) @PathVariable String productId) {
        return ApiResponseDto.success(productService.getProductWithCategories(productId));
    }

    @Operation(summary = "상품 수정", description = "판매자 본인이고 READY 상태일 때 상품 정보를 수정합니다.")
    @PutMapping("/{productId}")
    public ApiResponseDto<ProductCreateResult> updateProduct(@Parameter(description = "상품 ID", required = true) @PathVariable String productId,
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

    @GetMapping("/internal")
    public List<WishlistProductResponse> getProductInfos(@RequestBody List<String> productIds) {
        return productService.getProductInfos(productIds);
    }
}
