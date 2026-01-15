package com.dev_high.batch;

import com.dev_high.auction.application.AuctionLifecycleService;
import com.dev_high.auction.application.dto.AuctionProductProjection;
import com.dev_high.auction.domain.Auction;
import com.dev_high.auction.domain.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchHelper {

    private final AuctionRepository auctionRepository;
    private final AuctionLifecycleService lifecycleService;

    public RepeatStatus startAuctionsUpdate(StepContribution stepContribution,
                                            ChunkContext chunkContext) {

        List<AuctionProductProjection> targetIds = auctionRepository
                .bulkUpdateStartStatus();

        List<String> auctionIds = targetIds.stream()
                .map(AuctionProductProjection::getId)
                .distinct()

                .toList();
        List<String> productIds = targetIds.stream()
                .map(AuctionProductProjection::getProductId)
                .distinct()

                .toList();
        log.info("auction start target>> {}", targetIds.size());

        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        ec.put("startAuctionIds", auctionIds);
        ec.put("startProductIds", productIds);

        return RepeatStatus.FINISHED;
    }

    public RepeatStatus startAuctionsPostProcessing(StepContribution stepContribution,
                                                    ChunkContext chunkContext) {

        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        List<String> auctionIds =
                (List<String>) ec.get("startAuctionIds");

        List<String> productIds =
                (List<String>) ec.get("startProductIds");

        if (auctionIds != null && !auctionIds.isEmpty()) {
            // 여기서 알림, 후처리 로직 실행 ex) 해당 상품찜한 유저에게알림 발송
            lifecycleService.startBulkProcessing(auctionIds);
        }
        ec.remove("startAuctionIds");
        ec.remove("startProductIds");

        return RepeatStatus.FINISHED;
    }


    public RepeatStatus endAuctionsUpdate(StepContribution stepContribution,
                                          ChunkContext chunkContext) {
        List<AuctionProductProjection> targetIds = auctionRepository
                .bulkUpdateEndStatus();

        List<String> auctionIds = targetIds.stream()
                .map(AuctionProductProjection::getId)
                .distinct()
                .toList();

        List<String> productIds = targetIds.stream()
                .map(AuctionProductProjection::getProductId)
                .distinct()
                .toList();

        log.info("auction end target>> {}", targetIds.size());
        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        ec.put("endAuctionIds", auctionIds);
        ec.put("endProductIds", productIds);

        return RepeatStatus.FINISHED;

    }


    public RepeatStatus endAuctionsPostProcessing(StepContribution stepContribution,
                                                  ChunkContext chunkContext) {

        ExecutionContext ec = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        List<String> auctionIds =
                (List<String>) ec.get("endAuctionIds");

        List<String> productIds =
                (List<String>) ec.get("endProductIds");

        if (auctionIds != null && !auctionIds.isEmpty()) {
            lifecycleService.endBulkProcessing(auctionIds);
        }
        ec.remove("endAuctionIds");
        ec.remove("endProductIds");
        return RepeatStatus.FINISHED;
    }



}
