package com.dev_high.settlement.config;

import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementRepository;
import com.dev_high.settlement.domain.history.SettlementHistory;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig extends DefaultBatchConfiguration {
    @PersistenceContext private final EntityManagerFactory entityManagerFactory;
    private final SettlementRepository settlementRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AbstractItemCountingItemStreamItemReader registerItemReader;

    /**
     * 1. 정산 데이터 등록 Job
     *
     * @param jobRepository
     * @param transactionManager
     * @return
     */
    @Bean
    public Job registrationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("registrationJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // JobParameters가 항상 고유하도록 자동 증가
                .start(registrationStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step registrationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("registrationStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(registerItemReader)
                .processor(registerItemProcessor())
                .writer(settlementItemWriter(entityManagerFactory))
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, Settlement> registerItemProcessor() {
        return item
                -> settlementRepository.existsSettlementsByOrderId(item.getOrderId()) ? null
                : item;
    }

    @Bean
    public JpaItemWriter<Settlement> settlementItemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }


    /**
     * 2. 정산 처리 Job
     * Step 1: 예치금 서비스로 정산 요청
     * Step 2: 정산 상태 업데이트
     * Step 3: 정산 기록 저장
     */
    @Bean
    public Job processingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("processingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
//                .start(sendRequestStep(jobRepository, transactionManager))
                .start(updateStatusStep(jobRepository, transactionManager))
                .next(makeLogStep(jobRepository, transactionManager))
                .build();
    }
    /**
     * Step 1: 예치금 서비스로 계좌 정산 처리 요청
     */
    @Bean
    public Step sendRequestStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendRequestStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(sendRequestReader())
                .writer(sendRequestWriter())
                .listener(promotionListener())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> sendRequestReader() {
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("sendRequestReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate = :targetDate AND s.status = 'WAITING'")
                .parameterValues(Map.of("targetDate", LocalDate.now()))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemWriter<Settlement> sendRequestWriter() {
        return new ItemWriter<>() {
            private final RestTemplate restTemplate = new RestTemplate();
            private final String apiUrl = "http://localhost:8084/settlement/process";

            @Override
            public void write(Chunk<? extends Settlement> chunk) {
                StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

                // Step Context에서 처리된 Settlement ID 목록 가져오기
                @SuppressWarnings("unchecked")
                List<String> settlementIds = (List<String>) stepExecution.getExecutionContext()
                        .get("settlementIds");

                if (settlementIds == null) {
                    settlementIds = new ArrayList<>();
                }

                for (Settlement settlement : chunk) {
                    try {
                        Map<String, Object> request = Map.of(
                                "settlementId", settlement.getId(),
                                "orderId", settlement.getOrderId(),
                                "sellerId", settlement.getSellerId(),
                                "amount", settlement.getWinningAmount()
                        );

                        ResponseEntity<Map> response = restTemplate.postForEntity(
                                apiUrl,
                                request,
                                Map.class
                        );

                        if (response.getStatusCode().is2xxSuccessful()) {
                            settlementIds.add(settlement.getId());
                            log.info("정산 요청 성공 - Settlement ID: {}", settlement.getId());
                        } else {
                            log.error("정산 요청 실패 - Settlement ID: {}, Status: {}",
                                    settlement.getId(), response.getStatusCode());
                        }
                    } catch (Exception e) {
                        log.error("정산 요청 중 오류 발생 - Settlement ID: {}", settlement.getId(), e);
                    }
                }

                // Step Context에 저장 (Job Context로 승격될 예정)
                stepExecution.getExecutionContext().put("settlementIds", settlementIds);

                log.info("Step 1 완료 - 처리된 Settlement 개수: {}", settlementIds.size());
            }
        };
    }

    /**
     * Step Context의 데이터를 Job Context로 승격시키는 리스너
     */
    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"settlementIds"});
        return listener;
    }

    /**
     * Step 2: 정산 완료를 확인하고 정산 상태 변경
     */
    @Bean
    public Step updateStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("updateStatusStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(updateStatusReader(null))
                .processor(updateStatusProcessor())
                .writer(updateStatusWriter())
                .listener(updateStatusPromotionListener())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> updateStatusReader(@Value("#{jobExecutionContext['settlementIds']}") List<String> updatedIds) {

        if (updatedIds == null || updatedIds.isEmpty()) {
            log.warn("Step 3: 기록할 Settlement ID가 없습니다.");
            return () -> null;
        }

//        log.info("Step 2: {} 개의 Settlement 상태 업데이트 시작", processedIds.size());
//
//        return new JpaPagingItemReaderBuilder<Settlement>()
//                .name("updateStatusReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT s FROM Settlement s WHERE s.id IN :ids")
//                .parameterValues(Map.of("ids", processedIds))
//                .pageSize(10)
//                .build();

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("sendRequestReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate = :targetDate AND s.status = 'WAITING'")
                .parameterValues(Map.of("targetDate", LocalDate.now()))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, Settlement> updateStatusProcessor() {
        return settlement -> {
            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

            // Step Context에서 업데이트된 Settlement ID 목록 가져오기
            @SuppressWarnings("unchecked")
            List<String> settlementIds = (List<String>) stepExecution.getExecutionContext()
                    .get("settlementIds");

            if (settlementIds == null) {
                settlementIds = new ArrayList<>();
            }

            // 정산 완료 처리
            settlement.makeComplete();
            settlementIds.add(settlement.getId());

            // Step Context에 저장
            stepExecution.getExecutionContext().put("settlementIds", settlementIds);

            log.info("정산 상태 업데이트 - Settlement ID: {}, Status: {}",
                    settlement.getId(), settlement.getStatus());

            return settlement;
        };
    }

    @Bean
    public JpaItemWriter<Settlement> updateStatusWriter() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)  // merge 사용 (업데이트)
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener updateStatusPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"settlementIds"});
        return listener;
    }

    /**
     * Step 3: 정산 기록 테이블에 INSERT
     */
    @Bean
    public Step makeLogStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeLogStep", jobRepository)
                .<Settlement, SettlementHistory>chunk(10, transactionManager)
                .reader(makeLogReader(null))
                .processor(makeLogProcessor())
                .writer(makeLogWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> makeLogReader(@Value("#{jobExecutionContext['settlementIds']}") List<String> updatedIds) {
        if (updatedIds == null || updatedIds.isEmpty()) {
            log.warn("Step 3: 기록할 Settlement ID가 없습니다.");
            return () -> null;
        }

        log.info("Step 3: {} 개의 Settlement 로그 생성 시작", updatedIds.size());

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("makeLogReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.id IN :ids")
                .parameterValues(Map.of("ids", updatedIds))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, SettlementHistory> makeLogProcessor() {
        return SettlementHistory::fromSettlement;
    }

    @Bean
    public JpaItemWriter<SettlementHistory> makeLogWriter() {
        return new JpaItemWriterBuilder<SettlementHistory>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
}