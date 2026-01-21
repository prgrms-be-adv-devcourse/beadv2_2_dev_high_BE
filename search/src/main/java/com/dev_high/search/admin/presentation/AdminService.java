package com.dev_high.search.admin.presentation;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.common.kafka.event.auction.AuctionUpdateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductCreateSearchRequestEvent;
import com.dev_high.common.kafka.event.product.ProductUpdateSearchRequestEvent;
import com.dev_high.search.application.SearchService;
import com.dev_high.search.domain.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final JobLauncher jobLauncher;
    private final Job embeddingBackfillFullJob;
    private final Job embeddingBackfillMissingJob;
    private final SearchService searchService;

    public ApiResponseDto<Void> runEmbeddingBackfillFull() {
        try {
            jobLauncher.run(
                    embeddingBackfillFullJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters()
            );
            return ApiResponseDto.success("임베딩 전체 백필 배치가 시작되었습니다. (모든 상품 임베딩을 재생성합니다)", null);
        } catch (Exception e) {
            log.warn("임베딩 전체 백필 배치 실행 중 오류 발생: {}", e.getMessage());
            return ApiResponseDto.fail("임베딩 전체 백필 배치 실행에 실패했습니다.");
        }
    }

    public ApiResponseDto<Void> runEmbeddingBackfillMissing() {
        try {
            jobLauncher.run(
                    embeddingBackfillMissingJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters()
            );

            return ApiResponseDto.success(
                    "임베딩 누락 백필 배치가 시작되었습니다. (임베딩이 없는 상품만 처리합니다)",
                    null
            );
        } catch (Exception e) {
            log.warn("임베딩 누락 백필 배치 실행 중 오류 발생: {}", e.getMessage());

            return ApiResponseDto.fail(
                    "임베딩 누락 백필 배치 실행에 실패했습니다."
            );
        }
    }

    public ApiResponseDto<ProductDocument> createProduct(ProductCreateSearchRequestEvent request) {
        ProductDocument document = searchService.createProduct(request);
        return ApiResponseDto.success(document);
    }


    public ApiResponseDto<ProductDocument> updateByProduct(ProductUpdateSearchRequestEvent request) {
        ProductDocument document = searchService.updateByProduct(request);
        return ApiResponseDto.success(document);
    }

    public ApiResponseDto<ProductDocument> updateByAuction(AuctionUpdateSearchRequestEvent request) {
        ProductDocument document = searchService.updateByAuction(request);
        return ApiResponseDto.success(document);
    }

    public ApiResponseDto<Void> deleteByProduct(String productId) {
        searchService.deleteByProduct(productId);
        return ApiResponseDto.success(null);
    }
}
