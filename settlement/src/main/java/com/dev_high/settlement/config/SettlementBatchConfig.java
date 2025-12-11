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
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
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

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final KafkaEventPublisher publisher;
    private final SettlementRepository settlementRepository;
    // registrationJob에서 사용될 ItemReader는 외부에서 Bean으로 주입받습니다.
    private final AbstractItemCountingItemStreamItemReader<Settlement> registerItemReader;
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
     * 외부(e.g., Order 서비스)에서 수집된 주문 데이터를 읽어와 아직 시스템에 등록되지 않은 건에 대해
     * 새로운 정산(Settlement) 데이터를 생성하고 저장합니다.
     */
    @Bean
    public Job registrationJob() {
        return new JobBuilder("registrationJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Job 실행마다 파라미터 ID를 자동으로 증가시켜 재실행이 가능하게 함
                .start(registrationStep())
                .build();
    }

    /**
     * [Job 2] 정산 처리 Job
     * <p>
     * 정산일이 도래하고 상태가 'WAITING'인 정산 건들을 처리합니다.
     * <p>
     * <b>Step 1:</b> 예치금 서비스에 정산 처리를 요청합니다. (sendRequestStep)
     * <b>Step 2:</b> 요청 결과를 바탕으로 정산 상태를 'COMPLETED'로 업데이트합니다. (updateStatusStep)
     * <b>Step 3:</b> 처리된 정산 건에 대한 기록을 별도의 히스토리 테이블에 저장합니다. (makeLogStep)
     */
    @Bean
    public Job processingJob() {
        return new JobBuilder("processingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(sendRequestStep())
                .next(updateStatusStep())
                .next(makeLogStep())
                .build();
    }

    /**
     * [Job 3] 실패한 정산 재시도 Job
     * <p>
     * 이전에 실패했던(상태가 'FAILED') 정산 건들을 재시도합니다.
     * `processingJob`과 유사한 흐름이지만, 대상을 'FAILED' 상태의 정산 건으로 합니다.
     */
    @Bean
    public Job retryJob() {
        return new JobBuilder("retryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(sendRetryStep())
                .next(updateRetryStatusStep())
                .next(makeRetryLogStep())
                .build();
    }

    /**
     * [Job 4] 수동 처리 요청 알림 Job
     * <p>
     * 재시도가 여러 번(3회 이상) 실패하여 'FAILED' 상태로 남아있는 정산 건들을 찾아,
     * 상태를 'NOTIFIED'로 변경하고 관리자가 확인할 수 있도록 Kafka를 통해 알림을 보냅니다.
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

    // region 1. 정산 수집 및 등록 (Registration)
    @Bean
    public Step registrationStep() {
        return new StepBuilder("registrationStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(registerItemReader)
                .processor(registerItemProcessor())
                .writer(settlementItemWriter())
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, Settlement> registerItemProcessor() {
        // DB에 이미 존재하는 주문 ID인지 확인하여, 중복된 정산 데이터가 생성되는 것을 방지합니다.
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

    // region 2. 정산 처리 (Processing)
    /**
     * [Processing-Step 1] 예치금 서비스로 정산 처리를 요청하는 Step
     */
    @Bean
    public Step sendRequestStep() {
        return new StepBuilder("sendRequestStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(sendRequestReader())
                .writer(sendRequestWriter())
                .listener(promotionListener("processedIds")) // 성공한 ID를 다음 Step으로 전달
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> sendRequestReader() {
        // 정산일이 오늘이고 상태가 'WAITING'인 정산 건을 조회
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("sendRequestReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = 'WAITING'")
                .parameterValues(Map.of("start", LocalDate.now().atStartOfDay(), "end", LocalDate.now().atTime(LocalTime.MAX)))
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<Settlement> sendRequestWriter() {
        return chunk -> {
            List<Settlement> items = (List<Settlement>) chunk.getItems();
            List<String> successfullyProcessedIds = new ArrayList<>();
            items.forEach(s -> s.ready(true)); // 첫 정산 시도이므로 수수료 계산

            try {
                // 외부 API 호출
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, items, Map.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    List<String> ids = items.stream().map(Settlement::getId).toList();
                    successfullyProcessedIds.addAll(ids);
                    log.info("정산 요청 성공 - Settlement IDs: {}", ids);
                } else {
                    log.error("정산 요청 실패 - Status: {}, Items: {}", response.getStatusCode(), items.stream().map(Settlement::getId).toList());
                }
            } catch (Exception e) {
                log.error("정산 요청 중 오류 발생", e);
            }
            // StepExecution Context에 성공한 ID 목록 저장
            chunk.getStepExecution().getExecutionContext().put("processedIds", successfullyProcessedIds);
        };
    }

    /**
     * [Processing-Step 2] 정산 상태를 'COMPLETED'로 업데이트하는 Step
     */
    @Bean
    public Step updateStatusStep() {
        return new StepBuilder("updateStatusStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(updateStatusReader(null))
                .processor(updateStatusProcessor())
                .writer(updateStatusWriter())
                .listener(promotionListener("completedIds")) // 완료된 ID를 다음 Step으로 전달
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> updateStatusReader(@Value("#{jobExecutionContext['processedIds']}") List<String> processedIds) {
        // 이전 Step에서 성공적으로 처리된 정산 건들만 조회
        if (processedIds == null || processedIds.isEmpty()) {
            return new IteratorItemReader<>(new ArrayList<>());
        }
        List<Settlement> settlements = settlementRepository.findAllByIdIn(processedIds);
        return new IteratorItemReader<>(settlements);
    }
    
    @Bean
    @StepScope
    public ItemProcessor<Settlement, Settlement> updateStatusProcessor() {
        return item -> {
            item.updateStatus(SettlementStatus.COMPLETED); // 상태를 COMPLETED로 변경

            // StepExecution Context에 완료된 ID 저장
            StepExecution stepExecution = item.getStepExecution();
            @SuppressWarnings("unchecked")
            List<String> completedIds = (List<String>) stepExecution.getExecutionContext().get("completedIds");
            if (completedIds == null) completedIds = new ArrayList<>();
            completedIds.add(item.getId());
            stepExecution.getExecutionContext().put("completedIds", completedIds);
            return item;
        };
    }

    @Bean
    public JpaItemWriter<Settlement> updateStatusWriter() {
        // 기존 엔티티를 수정하므로 merge(기본값) 사용
        return new JpaItemWriterBuilder<Settlement>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    /**
     * [Processing-Step 3] 정산 히스토리를 기록하는 Step
     */
    @Bean
    public Step makeLogStep() {
        return new StepBuilder("makeLogStep", jobRepository)
                .<SettlementHistory, SettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(makeLogReader(null))
                .writer(makeLogWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<SettlementHistory> makeLogReader(@Value("#{jobExecutionContext['completedIds']}") List<String> completedIds) {
        // 정산이 완료된 건들을 조회하여 히스토리 객체로 변환
        if (completedIds == null || completedIds.isEmpty()) {
            return new IteratorItemReader<>(new ArrayList<>());
        }
        List<Settlement> settlements = settlementRepository.findAllByIdIn(completedIds);
        List<SettlementHistory> histories = settlements.stream().map(SettlementHistory::fromSettlement).toList();
        return new IteratorItemReader<>(histories);
    }

    @Bean
    public JpaItemWriter<SettlementHistory> makeLogWriter() {
        return new JpaItemWriterBuilder<SettlementHistory>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }
    // endregion

    // region 3. 실패한 정산 재시도 (Retry)
    /**
     * [Retry-Step 1] 실패한 정산 건에 대해 예치금 서비스로 재요청하는 Step
     */
    @Bean
    public Step sendRetryStep() {
        return new StepBuilder("sendRetryStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(sendRetryReader())
                .writer(sendRetryWriter())
                .listener(promotionListener("processedIds")) // 동일한 Key("processedIds")를 사용
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> sendRetryReader() {
        // 정산일이 오늘이고 상태가 'FAILED'인 정산 건을 조회
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("sendRetryReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.dueDate between :start and :end AND s.status = 'FAILED'")
                .parameterValues(Map.of("start", LocalDate.now().atStartOfDay(), "end", LocalDate.now().atTime(LocalTime.MAX)))
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<Settlement> sendRetryWriter() {
        // sendRequestWriter와 로직이 거의 동일하나, ready(false)로 호출
        return chunk -> {
            List<Settlement> items = (List<Settlement>) chunk.getItems();
            List<String> successfullyProcessedIds = new ArrayList<>();
            items.forEach(s -> s.ready(false)); // 재시도이므로 수수료 재계산 안 함

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, items, Map.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    List<String> ids = items.stream().map(Settlement::getId).toList();
                    successfullyProcessedIds.addAll(ids);
                    log.info("정산 재요청 성공 - Settlement IDs: {}", ids);
                } else {
                    log.error("정산 재요청 실패 - Status: {}, Items: {}", response.getStatusCode(), items.stream().map(Settlement::getId).toList());
                }
            } catch (Exception e) {
                log.error("정산 재요청 중 오류 발생", e);
            }
            chunk.getStepExecution().getExecutionContext().put("processedIds", successfullyProcessedIds);
        };
    }
    
    /**
     * [Retry-Step 2] 재시도 후 성공한 정산 건의 상태를 업데이트하는 Step
     */
    @Bean
    public Step updateRetryStatusStep() {
        return new StepBuilder("updateRetryStatusStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(updateStatusReader(null)) // processingJob의 Reader 재사용
                .processor(updateStatusProcessor())    // processingJob의 Processor 재사용
                .writer(updateStatusWriter())      // processingJob의 Writer 재사용
                .listener(promotionListener("completedIds")) // 동일한 Key("completedIds") 사용
                .build();
    }

    /**
     * [Retry-Step 3] 재시도 후 성공한 정산 건의 히스토리를 기록하는 Step
     */
    @Bean
    public Step makeRetryLogStep() {
        return new StepBuilder("makeRetryLogStep", jobRepository)
                .<SettlementHistory, SettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(makeLogReader(null)) // processingJob의 Reader 재사용
                .writer(makeLogWriter())     // processingJob의 Writer 재사용
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

    @Bean
    public ItemReader<Settlement> notificationReader() {
        // 재시도 횟수가 3회 이상이고 상태가 'FAILED'인 정산 건을 조회
        return new JpaPagingItemReaderBuilder<Settlement>()
                .name("notificationReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Settlement s WHERE s.tryCnt >= 3 AND s.status = 'FAILED'")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, SettlementConfirmRequest> notificationProcessor() {
        return s -> {
            s.updateStatus(SettlementStatus.NOTIFIED); // 상태를 'NOTIFIED'로 변경
            return SettlementConfirmRequest.fromSettlement(s); // Kafka로 보낼 DTO로 변환
        };
    }

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
    private ExecutionContextPromotionListener promotionListener(String keyToPromote) {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{keyToPromote});
        return listener;
    }
}