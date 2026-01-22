package com.dev_high.product.admin;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.product.application.dto.DashboardCategoryCountItem;
import com.dev_high.product.application.dto.ProductInfo;
import com.dev_high.product.admin.dto.AiProductGenerateRequest;
import com.dev_high.product.presentation.dto.ProductRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1:/api/v1}/admin/products")
@Tag(name = "ProductAdmin", description = "Product admin API")
public class ProductAdminController {

    private final ProductAdminService productAdminService;
    private final ProductAdminAiService productAdminAiService;

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

    @PostMapping(value = "/ai/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateAiProductsStream(
        @RequestBody AiProductGenerateRequest request
    ) {
        SseEmitter emitter = new SseEmitter(0L);
        var userInfo = com.dev_high.common.context.UserContext.get();
        emitter.onCompletion(() -> {});
        emitter.onTimeout(emitter::complete);
        emitter.onError(emitter::completeWithError);
        productAdminAiService.generateAiProductsAsync(request, userInfo)
            .whenComplete((products, ex) -> {
                try {
                    if (ex != null) {
                        emitter.send(SseEmitter.event()
                            .name("failed")
                            .data(Map.of("status", "failed", "error", ex.getMessage())));
                        emitter.completeWithError(ex);
                        return;
                    } else {
                        emitter.send(SseEmitter.event()
                            .name("completed")
                            .data(Map.of("status", "completed", "products", products)));
                    }
                    emitter.complete();
                } catch (IOException sendError) {
                    if (ex != null) {
                        emitter.completeWithError(ex);
                        return;
                    }
                    emitter.complete();
                }
            });

        return emitter;
    }


}
