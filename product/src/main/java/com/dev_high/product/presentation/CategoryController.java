package com.dev_high.product.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.application.CategoryService;
import com.dev_high.product.application.ProductService;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.domain.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1:/api/v1}/categories")
@Tag(name = "Category", description = "카테고리 API")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    //카테고리 목록 조회
    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리를 조회합니다.")
    @GetMapping
    public ApiResponseDto<List<Category>> getCategories() {
        return ApiResponseDto.success(categoryService.getCategories());
    }

    //카테고리별 상품 조회
    @Operation(summary = "카테고리별 상품 조회", description = "카테고리에 속한 상품을 페이지네이션으로 조회합니다.")
    @GetMapping("/{categoryId}")
    public ApiResponseDto<Page<ProductInfo>> getProductsByCategory(@Parameter(description = "카테고리 ID", required = true) @PathVariable String categoryId,
                                                                   Pageable pageable) {
        return ApiResponseDto.success(productService.getProductsByCategory(categoryId, pageable));
    }
}
