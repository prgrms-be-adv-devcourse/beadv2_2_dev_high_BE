package com.dev_high.batch.config;

import com.dev_high.batch.listener.SettlementJobExecutionListener;
import com.dev_high.batch.listener.SettlementRegistrationStepListener;
import com.dev_high.batch.processor.OrderToSettlementProcessor;
import com.dev_high.batch.processor.SettlementProcessor;
import com.dev_high.batch.reader.SettlementReader;
import com.dev_high.batch.reader.OrderSettlementReader;
import com.dev_high.batch.writer.SettlementWriter;
import com.dev_high.batch.writer.SettlementRegistrationWriter;
import com.dev_high.order.domain.WinningOrder;
import com.dev_high.settle.domain.settle.Settlement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SettlementReader settlementReader;
  private final SettlementProcessor settlementProcessor;
  private final SettlementWriter settlementWriter;
  private final OrderSettlementReader orderSettlementReader;
  private final OrderToSettlementProcessor orderToSettlementProcessor;
  private final SettlementRegistrationWriter settlementRegistrationWriter;
  private final SettlementRegistrationStepListener settlementRegistrationStepListener;
  private final SettlementJobExecutionListener settlementJobExecutionListener;

  @Bean
  @JobScope
  public List<Settlement> allSettlements() {
    return Collections.synchronizedList(new ArrayList<>());
  }

  @Bean
  @JobScope
  public List<Settlement> failedSettlements() { // 일정횟수 이상 실패한 정산목록 - 관리자 알림보내는용도
    return Collections.synchronizedList(new ArrayList<>());
  }

  @Bean
  public Job settlementJob() {
    // WAITING 정산 시: 주문→정산 등록 스텝 후 정산 처리 스텝 실행
    return new JobBuilder("settlementJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(registrationStep())
        .next(settlementStep())
        .listener(settlementJobExecutionListener)
        .build();
  }

  @Bean
  public Step registrationStep() {
    // CONFIRM_BUY 주문을 정산 엔티티로 적재하는 스텝
    return new StepBuilder("settlementRegistrationStep", jobRepository)
        .<WinningOrder, Settlement>chunk(50, transactionManager)
        .reader(orderSettlementReader)
        .processor(orderToSettlementProcessor)
        .writer(settlementRegistrationWriter)
        .listener(settlementRegistrationStepListener)
        .faultTolerant()
        .retry(CannotAcquireLockException.class)
        .retryLimit(5)
        .build();
  }

  @Bean
  public Step settlementStep() {
    // 정산 수행(결제/입금 호출 및 상태 업데이트)
    return new StepBuilder("settlementStep", jobRepository)
        .<Settlement, Settlement>chunk(50, transactionManager)
        .reader(settlementReader)
        .processor(settlementProcessor)
        .writer(settlementWriter)
        .faultTolerant()
        .retry(CannotAcquireLockException.class)
        .retryLimit(5)
        .build();
  }

    @Bean(name = "settleScheduler")
    public TaskScheduler settleScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1); //
        scheduler.setThreadNamePrefix("settlement-scheduler-");
        return scheduler;
    }
}
