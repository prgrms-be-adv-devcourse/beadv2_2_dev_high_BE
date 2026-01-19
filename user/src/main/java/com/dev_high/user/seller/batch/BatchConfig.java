package com.dev_high.user.seller.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final BatchHelper batchHelper;

    @Bean
    public Job sellerApproveJob() {
        return new JobBuilder("sellerApproveJob", jobRepository)
                .start(approveSellersStep())
                .build();
    }

    @Bean
    public Step approveSellersStep() {
        return new StepBuilder("approveSellersStep", jobRepository)
                .tasklet(batchHelper::approvePendingSellers, txManager)
                .build();
    }
}
