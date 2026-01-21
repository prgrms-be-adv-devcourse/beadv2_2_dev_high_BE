package com.dev_high.search.batch;

import com.dev_high.search.application.SearchService;
import com.dev_high.search.domain.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManger;
    private final ProductWriter productWriter;
    private final ProductReader fullReader;
    private final ProductReader missingReader;
    private final SearchService searchService;
    private static final int CHUNK_SIZE = 200;

    @Bean
    public Job embeddingBackfillFullJob() {
        return new JobBuilder("embeddingBackfillFullJob", jobRepository)
                .start(embeddingBackfillFullStep())
                .build();
    }

    @Bean
    public Step embeddingBackfillFullStep() {
        return new StepBuilder("embeddingBackfillFullStep", jobRepository)
                .<ProductDocument, ProductDocument>chunk(CHUNK_SIZE, txManger)
                .reader(fullReader)
                .processor(searchService::embedding)
                .writer(productWriter)
                .build();
    }

    @Bean
    public Job embeddingBackfillMissingJob() {
        return new JobBuilder("embeddingBackfillMissingJob", jobRepository)
                .start(embeddingBackfillMissingStep())
                .build();
    }

    @Bean
    public Step embeddingBackfillMissingStep() {
        return new StepBuilder("embeddingBackfillMissingStep", jobRepository)
                .<ProductDocument, ProductDocument>chunk(CHUNK_SIZE, txManger)
                .reader(missingReader)
                .processor(searchService::embedding)
                .writer(productWriter)
                .build();
    }
}