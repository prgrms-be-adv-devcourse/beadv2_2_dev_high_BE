package com.dev_high.settlement.config;

import com.dev_high.common.kafka.KafkaEventPublisher;
import com.dev_high.settlement.config.dto.SettlementConfirmRequest;
import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.domain.SettlementRepository;
import com.dev_high.settlement.domain.SettlementStatus;
import com.dev_high.settlement.domain.history.SettlementHistory;
import jakarta.persistence.EntityManagerFactory;
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
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig extends DefaultBatchConfiguration {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String SELLER_TOPIC = "settlement.confirm.seller";
    private static final String BUYER_TOPIC = "settlement.confirm.buyer";
    private static final String ADMIN_TOPIC = "settlement.confirm.admin";
    private final String apiUrl = "http://localhost:8084/settlement/process";

    private final KafkaEventPublisher publisher;
    private final EntityManagerFactory entityManagerFactory;
    private final SettlementRepository settlementRepository;
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


    /**
     * 2. 정산 처리 Job
     *
     * Step 1: 예치금 서비스로 정산 요청
     * Step 2: 정산 상태 업데이트
     * Step 3: 정산 기록 저장
     */
    @Bean
    public Job processingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("processingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
//                .start(sendRequestStep(jobRepository, transactionManager)) // TODO API 요청 결과[status]에 따라 분기
                .start(updateStatusStep(jobRepository, transactionManager))
                .next(makeLogStep(jobRepository, transactionManager))
                .build();
    }


    /**
     * 3. 실패한 정산 재시도 Job
     *
     * Step 1: 예치금 서비스로 정산 재시도 요청
     * Step 2: 정산 상태 업데이트
     * Step 3: 정산 기록 저장
     */
    @Bean
    public Job retryJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("retryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
//                .start(sendRetryStep(jobRepository, transactionManager)) // TODO API 요청 결과[status]에 따라 분기
                .start(updateRetryStatusStep(jobRepository, transactionManager))
                .next(makeRetryLogStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * 4. 연속 실패한 정산 수동 처리 요청 Job[3회]
     */
    @Bean
    public Job notificationJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("notificationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(notificationStep(jobRepository, transactionManager))
                .build();
    }

// region 1. 정산 수집 및 등록 Job

    /**
     * 정산 수집 및 등록 Step
     * <p>
     * =======================================================================================
     */
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
        return item -> settlementRepository.existsByOrderId(item.getOrderId()) ? null : item;
    }

    @Bean
    public JpaItemWriter<Settlement> settlementItemWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
// endregion 정산 수집 및 등록

//region 2. 정산 Job Step
    /**
     *
     * Step 1: 예치금 서비스로 계좌 정산 처리 요청
     */
    @Bean
    public Step sendRequestStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendRequestStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(sendRequestReader())
                .writer(sendRequestWriter())
                .listener(sendRequestPromotionListener())
                .build();
    }

    // region Step1. 정산 처리 요청 reader writer listener processor
    @Bean
    @StepScope
    public ItemReader<Settlement> sendRequestReader() {
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("sendRequestReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = 'WAITING'")
                .parameterValues(Map.of("start", LocalDate.now().atStartOfDay(), "end", LocalDate.now().atTime(LocalTime.MAX)))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemWriter<Settlement> sendRequestWriter() {
        return new ItemWriter<>() {
            private final String apiUrl = "http://localhost:8084/settlement/process";

            @Override
            public void write(Chunk<? extends Settlement> chunk) {
                StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

                // Step Context에서 처리된 Settlement ID 목록 가져오기
                @SuppressWarnings("unchecked")
                List<String> settlementIds = (List<String>) stepExecution.getExecutionContext().get("settlementIds");

                if (settlementIds == null) settlementIds = new ArrayList<>(); // Step간 데이터 공유를 위한 설정을 id로
                List<Settlement> settlements = List.of(chunk.getItems().toArray(Settlement[]::new)); // Context에 저장해 넘기고 얻어온다.
                settlements.forEach(s -> s.ready(true)); // 처음일 경우 정산 요청 당시에 계산하고 보낸다
                List<String> ids = settlements.stream().map(Settlement::getId).toList();

                processSettleRequest(settlements, settlementIds, ids, stepExecution);
            }
        };
    }

    /**
     * Step Context의 데이터를 Job Context로 승격시키는 리스너
     */
    @Bean
    public ExecutionContextPromotionListener sendRequestPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"settlementIds"});
        return listener;
    }
    // endregion 정산 처리 요청 reader writer listener processor

    /**
     * Step 2: 정산 완료를 확인하고 정산 상태 변경
     */
    @Bean
    public Step updateStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("updateStatusStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(updateStatusReader())//(null))
                .processor(updateStatusProcessor())
                .writer(updateStatusWriter())
                .listener(updateStatusPromotionListener())
                .build();
    }

    // region Step2. 정산 상태 변경 reader writer listener processor

    @Bean
//    @StepScope
    public ItemReader<Settlement> updateStatusReader(
//            @Value("#{jobExecutionContext['settlementIds']}") List<String> updatedIds
    ) {

//        if (updatedIds == null || updatedIds.isEmpty()) {
//            log.warn("Step 3: 기록할 Settlement ID가 없습니다.");
//            return () -> null;
//        }

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
                .name("updateStatusReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = 'WAITING'")
                .parameterValues(Map.of("start", LocalDate.now().atStartOfDay(), "end", LocalDate.now().atTime(LocalTime.MAX)))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, Settlement> updateStatusProcessor() {
        return settlement -> {
            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

            // Step Context에서 업데이트된 Settlement ID 목록 가져오기
            List<String> settlementIds = (List<String>) stepExecution.getExecutionContext().get("settlementIds");

            return updateStatus(settlement, settlementIds, stepExecution);
        };
    }

    @Bean
    public JpaItemWriter<Settlement> updateStatusWriter() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener updateStatusPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"settlementIds"});
        return listener;
    }
    // endregion Step2. 정산 상태 변경 reader writer listener processor

    /**
     * Step 3: 정산 기록 테이블에 INSERT
     */
    @Bean
    public Step makeLogStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeLogStep", jobRepository)
                .<SettlementHistory, SettlementHistory>chunk(10, transactionManager)
                .reader(makeLogReader(null))
                .writer(makeLogWriter())
                .build();
    }

    // region Step 3: 정산 기록 테이블에 INSERT reader writer listener processor
    @Bean
    @StepScope
    public ItemReader<SettlementHistory> makeLogReader(@Value("#{jobExecutionContext['settlementIds']}") List<String> updatedIds) {
        return readLogTargets(updatedIds);
    }

    @Bean
    public JpaItemWriter<SettlementHistory> makeLogWriter() {
        return new JpaItemWriterBuilder<SettlementHistory>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    // endregion Step 3: 정산 기록 테이블에 INSERT

//endregion 2. 정산 처리 Job

//region 3. 실패한 정산 재시도 Job
    /**
     * Step 1: 예치금 서비스로 계좌 정산 처리 요청
     */
    @Bean
    public Step sendRetryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendRetryStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(sendRetryReader())
                .writer(sendRetryWriter())
                .listener(sendRetryPromotionListener())
                .build();
    }

    // region Step1. 정산 처리 요청 reader writer processor
    @Bean
    @StepScope
    public ItemReader<Settlement> sendRetryReader() {
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("sendRequestReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = 'FAILED'")
                .parameterValues(Map.of("start", LocalDate.now().atStartOfDay(), "end", LocalDate.now().atTime(LocalTime.MAX)))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemWriter<Settlement> sendRetryWriter() {
        return chunk -> {
            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

            // Step Context에서 처리된 Settlement ID 목록 가져오기
            @SuppressWarnings("unchecked")
            List<String> settlementIds = (List<String>) stepExecution.getExecutionContext().get("settlementIds");

            if (settlementIds == null) settlementIds = new ArrayList<>();

            List<Settlement> settlements = List.of(chunk.getItems().toArray(Settlement[]::new));
            settlements.forEach(s -> s.ready(false));
            List<String> ids = settlements.stream().map(Settlement::getId).toList();

            processSettleRequest(settlements, settlementIds, ids, stepExecution);
        };
    }

    /**
     * Step Context의 데이터를 Job Context로 승격시키는 리스너
     */
    @Bean
    public ExecutionContextPromotionListener sendRetryPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"settlementIds"});
        return listener;
    }
    //endregion

    /**
     * Step 2: 정산 완료를 확인하고 정산 상태 변경
     */
    @Bean
    public Step updateRetryStatusStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("updateRetryStatusStep", jobRepository)
                .<Settlement, Settlement>chunk(10, transactionManager)
                .reader(updateRetryStatusReader())//(null))
                .processor(updateRetryStatusProcessor())
                .writer(updateRetryStatusWriter())
                .listener(updateRetryStatusPromotionListener())
                .build();
    }

    //region Step 2: 정산 완료를 확인하고 정산 상태 변경 reader writer listener processor
    @Bean
//    @StepScope
    public ItemReader<Settlement> updateRetryStatusReader(
//            @Value("#{jobExecutionContext['settlementIds']}") List<String> updatedIds
    ) {

//        if (updatedIds == null || updatedIds.isEmpty()) {
//            log.warn("Step 3: 기록할 Settlement ID가 없습니다.");
//            return () -> null;
//        }

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
                .name("updateStatusReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = 'FAILED'")
                .parameterValues(Map.of("start", LocalDate.now().atStartOfDay(), "end", LocalDate.now().atTime(LocalTime.MAX)))
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, Settlement> updateRetryStatusProcessor() {
        return settlement -> {
            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

            // Step Context에서 업데이트된 Settlement ID 목록 가져오기
            @SuppressWarnings("unchecked")
            List<String> settlementIds = (List<String>) stepExecution.getExecutionContext().get("settlementIds");

            return updateStatus(settlement, settlementIds, stepExecution);
        };
    }

    @Bean
    public JpaItemWriter<Settlement> updateRetryStatusWriter() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(false)
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener updateRetryStatusPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"settlementIds"});
        return listener;
    }
    //endregion Step 2: 정산 완료를 확인하고 정산 상태 변경

    /**
     * Step 3: 정산 기록 테이블에 INSERT
     */
    @Bean
    public Step makeRetryLogStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeLogStep", jobRepository)
                .<SettlementHistory, SettlementHistory>chunk(10, transactionManager)
                .reader(makeRetryLogReader(null))
                .writer(makeRetryLogWriter())
                .build();
    }

    //region Step 3: 정산 기록 테이블에 INSERT reader writer listener processor

    @Bean
    @StepScope
    public ItemReader<SettlementHistory> makeRetryLogReader(@Value("#{jobExecutionContext['settlementIds']}") List<String> updatedIds) {
        return readLogTargets(updatedIds);
    }

    @Bean
    public JpaItemWriter<SettlementHistory> makeRetryLogWriter() {
        return new JpaItemWriterBuilder<SettlementHistory>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
    //endregion Step 3: 정산 기록 테이블에 INSERT  reader writer listener processor

//endregion 3. 실패한 정산 재시도 Job

    //region 4. notificationStep
    @Bean
    public Step notificationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("notificationStep", jobRepository)
                .<Settlement, SettlementConfirmRequest>chunk(10, transactionManager)
                .reader(notificationReader())
                .processor(notificationProcessor())
                .writer(sendNotifyWriter())
                .build();

    }

    @Bean
    public ItemReader<Settlement> notificationReader() {
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("notificationReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.tryCnt >= 3 AND s.status = 'FAILED'")
                .pageSize(10)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, SettlementConfirmRequest> notificationProcessor() {
        return s -> {
            s.setStatus(SettlementStatus.NOTIFIED);
            return SettlementConfirmRequest.fromSettlement(s);
        };
    }

    @Bean
    public ItemWriter<SettlementConfirmRequest> sendNotifyWriter() {
        return chunk -> {

            log.info("정산 확정 이벤트 발행 시작 - {}건", chunk.size());

            chunk.forEach(request -> {
                // 판매자
                publisher.publish(SELLER_TOPIC, request);

//                // 관리자
//                publisher.publish(
//                        ADMIN_TOPIC,
//                        request.getId(),
//                        payload
//                );
            });

            log.info("정산 확정 이벤트 발행 완료 - {}건 처리됨", chunk.size());
        };
    }
//endregion

    /**
     * 정산 처리 요청(재요청 포함)하는 메소드 extracted - try 횟수
     * @param settlements
     * @param settlementIds
     * @param ids
     * @param stepExecution
     */
    private void processSettleRequest(List<Settlement> settlements, List<String> settlementIds, List<String> ids, StepExecution stepExecution) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, settlements, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                settlementIds.addAll(ids);
                log.info("정산 요청 성공 - Settlement ID: {}", ids);
            } else {
                settlements.forEach(settlement -> settlement.updateStatus(SettlementStatus.FAILED));
                settlementRepository.saveAll(settlements);
                log.error("정산 요청 실패 - Settlement ID: {}, Status: {}", ids, response.getStatusCode());
            }
        } catch (Exception e) {
            settlements.forEach(settlement -> settlement.updateStatus(SettlementStatus.FAILED));
            settlementRepository.saveAll(settlements);
            log.error("정산 요청 중 오류 발생 - Settlement ID: {}", ids, e);
        }

        // Step Context에 저장 (Job Context로 승격될 예정)
        stepExecution.getExecutionContext().put("settlementIds", settlementIds);

        log.info("Step 1 완료 - 처리된 Settlement 개수: {}", settlementIds.size());
    }

    private static Settlement updateStatus(Settlement settlement, List<String> settlementIds, StepExecution stepExecution) {
        if (settlementIds == null) {
            settlementIds = new ArrayList<>();
        }

        // 정산 완료 처리
        settlement.updateStatus(SettlementStatus.COMPLETED);
        settlementIds.add(settlement.getId());

        // Step Context에 저장
        stepExecution.getExecutionContext().put("settlementIds", settlementIds);

        log.info("정산 상태 업데이트 - Settlement ID: {}, Status: {}", settlement.getId(), settlement.getStatus());

        return settlement;
    }

    private ItemReader<SettlementHistory> readLogTargets(List<String> updatedIds) {
        if (updatedIds == null || updatedIds.isEmpty()) {
            log.warn("Step 3: 기록할 Settlement ID가 없습니다.");
            return () -> null;
        }

        log.info("Step 3: {} 개의 Settlement 로그 생성 시작", updatedIds.size());
        List<Settlement> settlements = settlementRepository.findAllByIdIn(updatedIds);
        Iterator<SettlementHistory> iterator = settlements.stream().map(SettlementHistory::fromSettlement).iterator();

        return () -> iterator.hasNext() ? iterator.next() : null;
    }

}