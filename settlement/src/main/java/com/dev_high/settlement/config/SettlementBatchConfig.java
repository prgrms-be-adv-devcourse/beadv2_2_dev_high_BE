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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 정산 관련 배치 작업을 설정하는 클래스입니다.
 * <p>
 * <ul>
 *     <li><b>registrationJob:</b> 새로운 정산 데이터를 수집하여 등록합니다.</li>
 *     <li><b>processingJob:</b> 정산일이 도래한 정산 건을 처리합니다.</li>
 *     <li><b>retryJob:</b> 실패한 정산 건을 재처리합니다.</li>
 *     <li><b>notificationJob:</b> 여러 번 실패한 정산 건에 대해 관리자에게 알림을 보냅니다.</li>
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementBatchConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementRepository settlementRepository;
    private final KafkaEventPublisher publisher;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final int CHUNK_SIZE = 10;
    private static final String SELLER_TOPIC = "settlement.confirm.seller";
    private final String apiUrl = "http://localhost:8084/settlement/process";


    // =================================================================================================================
    // == JOBS
    // =================================================================================================================

    /**
     * [Job 1] 정산 데이터 등록 Job
     * <p>
     * 매일 특정 시간에 실행되어, 아직 시스템에 등록되지 않은 주문 건을 읽어와 정산(Settlement) 데이터로 변환하고 저장합니다.
     */
    @Bean
    public Job registrationJob(ItemReader<Settlement> orderItemReader) {
        return new JobBuilder("registrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(registrationStep(orderItemReader))
                .build();
    }

    /**
     * [Job 2] 정산 처리 Job
     * <p>
     * 매일 정해진 시간에 실행되어, 정산일이 도래하고 상태가 'WAITING'인 정산 건들을 처리합니다.
     * <p>
     * <b>Step 1:</b> 예치금 서비스에 정산 처리를 요청합니다. (sendRequestStep)
     * <b>Step 2:</b> 요청 결과를 바탕으로 정산 상태를 'COMPLETED' 또는 'FAILED'로 업데이트합니다. (updateStatusStep)
     * <b>Step 3:</b> 처리된 정산 건에 대한 기록을 별도의 히스토리 테이블에 저장합니다. (makeLogStep)
     */
    @Bean
    public Job processingJob() {
        return new JobBuilder("processingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(sendRequestStep("sendRequestStepForProcessing", SettlementStatus.WAITING, true))
                .next(updateStatusStep("updateStatusStepForProcessing"))
                .next(makeLogStep("makeLogStepForProcessing"))
                .build();
    }

    /**
     * [Job 3] 실패한 정산 재시도 Job
     * <p>
     * 정기적으로 실행되어, 이전에 실패했던(상태가 'FAILED') 정산 건들을 재시도합니다.
     * `processingJob`과 동일한 Step들을 수행하지만, 대상을 'FAILED' 상태의 정산 건으로 합니다.
     */
    @Bean
    public Job retryJob() {
        return new JobBuilder("retryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(sendRequestStep("sendRequestStepForRetry", SettlementStatus.FAILED, false))
                .next(updateStatusStep("updateStatusStepForRetry"))
                .next(makeLogStep("makeLogStepForRetry"))
                .build();
    }

    /**
     * [Job 4] 수동 처리 요청 알림 Job
     * <p>
     * 재시도가 여러 번(3회 이상) 실패하여 'FAILED' 상태로 남아있는 정산 건들을 찾아,
     * 해당 건들의 상태를 'NOTIFIED'로 변경하고 관리자가 확인할 수 있도록 알림(Kafka Event)을 보냅니다.
     */
    @Bean
    public Job notificationJob() {
        return new JobBuilder("notificationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(notificationStep())
                .build();
    }


    // =================================================================================================================
    // == STEPS
    // =================================================================================================================

    // region 1. 정산 수집 및 등록(Registration)
    @Bean
    public Step registrationStep(ItemReader<Settlement> orderItemReader) {
        return new StepBuilder("registrationStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(orderItemReader)
                .processor(registerItemProcessor())
                .writer(settlementItemWriter())
                .build();
    }

    /**
     * DB에 이미 존재하는 주문 ID인지 확인하여, 중복된 정산 데이터가 생성되는 것을 방지합니다.
     *
     * @return 존재하지 않으면 item을, 존재하면 null을 반환하여 writer에서 제외시킵니다.
     */
    @Bean
    public ItemProcessor<Settlement, Settlement> registerItemProcessor() {
        return item -> settlementRepository.existsByOrderId(item.getOrderId()) ? null : item;
    }

    @Bean
    public JpaItemWriter<Settlement> settlementItemWriter() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true) // 새로운 엔티티를 저장하므로 persist 사용
                .build();
    }
    // endregion

    // region 2 & 3. 정산 처리 및 재시도 (Processing & Retry) - Refactored
    /**
     * [Step 1] 예치금 서비스로 정산 처리를 요청하는 Step입니다.
     * `processingJob`과 `retryJob`에서 공통으로 사용됩니다.
     *
     * @param stepName Step의 이름 (고유해야 함)
     * @param status   조회할 정산 상태 (WAITING or FAILED)
     * @param isFirst  첫 처리 여부 (수수료 계산에 사용)
     * @return Step 객체
     */
    @Bean
    public Step sendRequestStep(String stepName, SettlementStatus status, boolean isFirst) {
        return new StepBuilder(stepName, jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(sendRequestReader(null, status))
                .writer(sendRequestWriter(isFirst))
                .listener(promotionListener("settlementIds")) // Step 간 데이터 공유를 위해 Listener 등록
                .build();
    }

    /**
     * 정산 대상을 DB에서 읽어오는 Reader입니다.
     *
     * @param status 조회할 정산 상태
     * @return ItemReader 객체
     */
    @Bean
    @StepScope
    public ItemReader<Settlement> sendRequestReader(@Value("#{stepExecution.stepName}") String readerName, SettlementStatus status) {
        Map<String, Object> params = Map.of(
                "start", LocalDate.now().atStartOfDay(),
                "end", LocalDate.now().atTime(LocalTime.MAX),
                "status", status);

        return new JpaPagingItemReaderBuilder<Settlement>()
                .name(readerName)
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = :status")
                .parameterValues(params)
                .pageSize(CHUNK_SIZE)
                .build();
    }

    /**
     * 외부 예치금 서비스 API를 호출하여 정산 처리를 요청하는 Writer입니다.
     * API 호출 결과에 따라 성공한 ID 목록을 `ExecutionContext`에 저장하여 다음 Step으로 전달합니다.
     *
     * @param isFirst 첫 처리 여부
     * @return ItemWriter 객체
     */
    @Bean
    @StepScope
    public ItemWriter<Settlement> sendRequestWriter(@Value("#{stepExecutionContext['isFirst']}") boolean isFirst) {
        return chunk -> {
            StepExecution stepExecution = chunk.getStepExecution();
            List<Settlement> items = (List<Settlement>) chunk.getItems();
            List<String> successfullyProcessedIds = new ArrayList<>();

            items.forEach(s -> s.ready(isFirst));

            try {
                // 외부 API 호출
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, items, Map.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    successfullyProcessedIds.addAll(items.stream().map(Settlement::getId).toList());
                    log.info("정산 요청 성공 - Settlement IDs: {}", successfullyProcessedIds);
                } else {
                    log.error("정산 요청 실패 - Status: {}, Items: {}", response.getStatusCode(), items.stream().map(Settlement::getId).toList());
                }
            } catch (Exception e) {
                log.error("정산 요청 중 오류 발생", e);
            }

            // 성공한 ID 목록을 StepExecution Context에 저장
            stepExecution.getExecutionContext().put("settlementIds", successfullyProcessedIds);
        };
    }

    /**
     * [Step 2] 정산 처리 요청 결과를 바탕으로 정산 상태를 업데이트하는 Step입니다.
     *
     * @param stepName Step의 이름
     * @return Step 객체
     */
    @Bean
    public Step updateStatusStep(String stepName) {
        return new StepBuilder(stepName, jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(updateStatusReader(null))
                .processor(updateStatusProcessor())
                .writer(settlementItemWriterForUpdate())
                .listener(promotionListener("updatedSettlementIds"))
                .build();
    }

    /**
     * 이전 Step(`sendRequestStep`)에서 성공적으로 처리된 정산 건들만 읽어오는 Reader입니다.
     * `jobExecutionContext`에 저장된 ID 목록을 사용합니다.
     */
    @Bean
    @StepScope
    public ItemReader<Settlement> updateStatusReader(@Value("#{jobExecutionContext['settlementIds']}") List<String> settlementIds) {
        if (settlementIds == null || settlementIds.isEmpty()) {
            log.warn("업데이트할 정산 건이 없습니다.");
            return new IteratorItemReader<>(new ArrayList<>());
        }
        List<Settlement> settlements = settlementRepository.findAllByIdIn(settlementIds);
        return new IteratorItemReader<>(settlements);
    }

    /**
     * 읽어온 정산 건의 상태를 'COMPLETED'로 변경하는 Processor입니다.
     * 처리된 ID는 다음 Step(`makeLogStep`)으로 전달하기 위해 `ExecutionContext`에 저장합니다.
     */
    @Bean
    @StepScope
    public ItemProcessor<Settlement, Settlement> updateStatusProcessor() {
        return item -> {
            item.updateStatus(SettlementStatus.COMPLETED);

            // 처리된 ID를 Context에 저장
            StepExecution stepExecution = item.getStepExecution();
            @SuppressWarnings("unchecked")
            List<String> updatedIds = (List<String>) stepExecution.getExecutionContext().get("updatedSettlementIds");
            if (updatedIds == null) updatedIds = new ArrayList<>();
            updatedIds.add(item.getId());
            stepExecution.getExecutionContext().put("updatedSettlementIds", updatedIds);

            return item;
        };
    }

    /**
     * 변경된 정산 데이터를 DB에 업데이트하는 Writer입니다.
     */
    @Bean
    public JpaItemWriter<Settlement> settlementItemWriterForUpdate() {
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .build(); // usePersist=false (merge)가 기본값
    }

    /**
     * [Step 3] 처리된 정산 건의 히스토리를 저장하는 Step입니다.
     *
     * @param stepName Step의 이름
     * @return Step 객체
     */
    @Bean
    public Step makeLogStep(String stepName) {
        return new StepBuilder(stepName, jobRepository)
                .<SettlementHistory, SettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(makeLogReader(null))
                .writer(settlementHistoryItemWriter())
                .build();
    }

    /**
     * 이전 Step(`updateStatusStep`)에서 상태가 업데이트된 정산 건들의 ID를 받아,
     * 해당 정산 건들을 조회하고 `SettlementHistory`로 변환하여 읽어오는 Reader입니다.
     */
    @Bean
    @StepScope
    public ItemReader<SettlementHistory> makeLogReader(@Value("#{jobExecutionContext['updatedSettlementIds']}") List<String> updatedIds) {
        if (updatedIds == null || updatedIds.isEmpty()) {
            log.warn("기록할 정산 건이 없습니다.");
            return new IteratorItemReader<>(new ArrayList<>());
        }
        List<Settlement> settlements = settlementRepository.findAllByIdIn(updatedIds);
        List<SettlementHistory> histories = settlements.stream().map(SettlementHistory::from).toList();
        return new IteratorItemReader<>(histories);
    }

    @Bean
    public JpaItemWriter<SettlementHistory> settlementHistoryItemWriter() {
        return new JpaItemWriterBuilder<SettlementHistory>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
    // endregion

    // region 4. 수동 처리 알림 (Notification)
    @Bean
    public Step notificationStep() {
        return new StepBuilder("notificationStep", jobRepository)
                .<Settlement, SettlementConfirmRequest>chunk(CHUNK_SIZE, transactionManager)
                .reader(notificationReader())
                .processor(notificationProcessor())
                .writer(sendNotifyWriter())
                .build();
    }

    /**
     * 재시도 횟수(tryCnt)가 3회 이상이고 상태가 'FAILED'인 정산 건을 조회하는 Reader입니다.
     */
    @Bean
    public ItemReader<Settlement> notificationReader() {
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("notificationReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.tryCnt >= 3 AND s.status = 'FAILED'")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    /**
     * 조회된 정산 건의 상태를 'NOTIFIED'로 변경하고, Kafka로 보낼 DTO로 변환하는 Processor입니다.
     */
    @Bean
    public ItemProcessor<Settlement, SettlementConfirmRequest> notificationProcessor() {
        return s -> {
            s.updateStatus(SettlementStatus.NOTIFIED); // 상태 변경
            return SettlementConfirmRequest.from(s); // DTO 변환
        };
    }

    /**
     * Kafka로 정산 확정(수동 처리 필요) 이벤트를 발행하는 Writer입니다.
     */
    @Bean
    public ItemWriter<SettlementConfirmRequest> sendNotifyWriter() {
        return chunk -> {
            log.info("정산 확정(수동 처리) 이벤트 발행 시작 - {}건", chunk.size());
            chunk.forEach(request -> publisher.publish(SELLER_TOPIC, request));
            log.info("정산 확정 이벤트 발행 완료 - {}건 처리됨", chunk.size());
        };
    }
    // endregion


    // =================================================================================================================
    // == COMMON COMPONENTS
    // =================================================================================================================

    /**
     * StepExecution Context의 특정 키 값을 JobExecution Context로 승격시키는 Listener를 생성합니다.
     * 이를 통해 Step 간 데이터 공유가 가능해집니다.
     *
     * @param keyToPromote JobExecution Context로 승격시킬 키
     * @return ExecutionContextPromotionListener 객체
     */
    @Bean
    public ExecutionContextPromotionListener promotionListener(String keyToPromote) {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{keyToPromote});
        return listener;
    }
}