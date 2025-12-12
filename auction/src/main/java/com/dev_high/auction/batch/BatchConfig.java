package com.dev_high.auction.batch;

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
  public Job auctionJob() {
    return new JobBuilder("auctionJob", jobRepository)
        .start(startAuctionsUpdateStep())
        .next(startAuctionsPostProcessingStep())
        .next(endAuctionsUpdateStep())
        .next(endAuctionsPostProcessingStep())
        .build();
  }

  @Bean
  public Step startAuctionsUpdateStep() {
    return new StepBuilder("startAuctionsUpdateStep", jobRepository)
        .tasklet(batchHelper::startAuctionsUpdate, txManager)
        .build();
  }

  @Bean
  public Step startAuctionsPostProcessingStep() {
    return new StepBuilder("startAuctionsPostProcessingStep", jobRepository)
        .tasklet(batchHelper::startAuctionsPostProcessing, txManager)
        .build();
  }

  @Bean
  public Step endAuctionsUpdateStep() {
    return new StepBuilder("endAuctionsUpdateStep", jobRepository)
        .tasklet(batchHelper::endAuctionsUpdate, txManager)
        .build();
  }

  @Bean
  public Step endAuctionsPostProcessingStep() {
    return new StepBuilder("endAuctionsPostProcessingStep", jobRepository)
        .tasklet(batchHelper::endAuctionsPostProcessing, txManager)
        .build();
  }
}