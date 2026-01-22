package com.dev_high.search.admin.application;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.search.admin.presentation.AdminService;
import com.dev_high.search.domain.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/search")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/batch/embedding-backfill/full/run")
    public ApiResponseDto<Void> runFull() {
        return adminService.runEmbeddingBackfillFull();
    }

    @PostMapping("/batch/embedding-backfill/missing/run")
    public ApiResponseDto<Void> runMissing() {
        return adminService.runEmbeddingBackfillMissing();
    }

    @PostMapping("/document/create")
    public ApiResponseDto<ProductDocument> createProduct(@RequestBody ProductCreateSearchRequestEvent request) {
        return adminService.createProduct(request);
    }

    @PutMapping("/document/update/product")
    public ApiResponseDto<ProductDocument> updateByProduct(@RequestBody ProductUpdateSearchRequestEvent request) {
        return adminService.updateByProduct(request);
    }

    @PutMapping("/document/update/auction")
    public ApiResponseDto<ProductDocument> updateByAuction(@RequestBody AuctionUpdateSearchRequestEvent request) {
        return adminService.updateByAuction(request);
    }

    @DeleteMapping("/document/delete/{productId}")
    public ApiResponseDto<Void> deleteByProduct(@PathVariable String productId) {
        return adminService.deleteByProduct(productId);
    }
}
