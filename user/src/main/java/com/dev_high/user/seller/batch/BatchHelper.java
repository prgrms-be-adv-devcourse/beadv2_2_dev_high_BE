package com.dev_high.user.seller.batch;

import com.dev_high.user.seller.application.SellerService;
import com.dev_high.user.seller.application.dto.SellerApproveResult;
import com.dev_high.user.seller.domain.Seller;
import com.dev_high.user.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchHelper {

    private final SellerService sellerService;
    private final SellerRepository sellerRepository;

    @Value("${seller.batch.approve-limit:500}")
    private int approveLimit;

    public RepeatStatus approvePendingSellers(StepContribution contribution,
                                              ChunkContext chunkContext) {

        List<Seller> targets = sellerRepository.findPendingSellers(approveLimit);

        SellerApproveResult result =
                sellerService.approveSellers(targets, "SYSTEM");

        log.info("sellerApprove batch finished. approved={}, roleInserted={}, skipped={}, total={}",
                result.approved(), result.roleInserted(), result.skipped(), result.total());

        return RepeatStatus.FINISHED;
    }
}
