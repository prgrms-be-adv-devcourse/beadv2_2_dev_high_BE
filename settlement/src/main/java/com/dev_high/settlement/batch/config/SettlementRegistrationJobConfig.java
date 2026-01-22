package com.dev_high.settlement.batch.config;

import com.dev_high.settlement.batch.listener.RegistrStepExecutionListener;
import com.dev_high.settlement.batch.processor.OrderToSettlementProcessor;
import com.dev_high.settlement.batch.reader.OrderReader;
import com.dev_high.settlement.batch.writer.OrderToSettlementWriter;
import com.dev_high.settlement.domain.Settlement;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementRegistrationJobConfig {

  private final PlatformTransactionManager transactionManager;
  private final OrderReader orderReader;
  private final OrderToSettlementProcessor processor;
  private final OrderToSettlementWriter writer;
  private final RegistrStepExecutionListener stepExecutionListener;
  private final JobRepository jobRepository;

  @Bean
  public Job registrationJob() {
    return new JobBuilder("registrationJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(registrationStep())
        .build();
  }

   
  @Bean
  public Step registrationStep() {
    return new StepBuilder("registrationStep", jobRepository)  //job에 step 등록
        .<SettlementRegisterRequest, Settlement>chunk(50, transactionManager) //50개단위로 chunk
        .reader(orderReader) // 데이터 가져오는 수행
        .processor(processor) // 데이터 로직 처리 수행
        .writer(writer) // db 반영
        .listener(stepExecutionListener) //스텝 전/후 처리
        .faultTolerant()
        .retry(CannotAcquireLockException.class)
        .retryLimit(5)
        .build();
  }


}

