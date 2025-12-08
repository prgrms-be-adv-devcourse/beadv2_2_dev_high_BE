package com.dev_high.order.batch;

import com.dev_high.common.dto.ApiResponseDto;
import com.dev_high.order.domain.Order;
import com.dev_high.order.domain.OrderRepository;
import com.dev_high.order.domain.OrderStatus;
import com.dev_high.settlement.presentation.dto.SettlementRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class OrderBatchConfig {

    @Bean
    public Job sendConfirmedOrder(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  OrderRepository orderRepository) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(findTargets(jobRepository, transactionManager, orderRepository))
                .next(convertToEntity(jobRepository, transactionManager))
                .next(sendToService(jobRepository, transactionManager))
                .build();
    }

//    @Bean
//    public Step testJob(JobRepository jobRepository,
//                        PlatformTransactionManager transactionManager) {
//        log.info("success");
//        return new StepBuilder("test", jobRepository)
//                .tasklet(((contribution, chunkContext) -> {
//                    System.out.println(LocalDateTime.now());
//                    return RepeatStatus.FINISHED;
//                }), transactionManager).build();
//    }

    @Bean
    @StepScope
    public Step findTargets(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            OrderRepository orderRepository) {
        return new StepBuilder("findTargets", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    List<Order> targets = orderRepository.findAllByPayCompleteDateAndStatus(LocalDate.now().atStartOfDay(), LocalDate.now().atTime(LocalTime.MAX), OrderStatus.CONFIRM_BUY);
                    chunkContext.getStepContext().getStepExecution().getExecutionContext().put("targets", targets);
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Step convertToEntity(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("convertToEntity", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    List<Order> targets = (List<Order>) chunkContext.getAttribute("targets");
                    List<SettlementRegisterRequest> data = targets.stream().map(Order::toSettlementRequest).toList();
                    chunkContext.getStepContext().getStepExecution().getExecutionContext().put("data", data);
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Step sendToService(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendToService", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    List<SettlementRegisterRequest> data = (List<SettlementRegisterRequest>) chunkContext.getAttribute("data");

                    HttpHeaders headers = new org.springframework.http.HttpHeaders();
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String token = authentication.getCredentials().toString();
//        headers.setBearerAuth(token);

                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                    HttpEntity<List<SettlementRegisterRequest>> request = new HttpEntity<>(data, headers);

                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.postForEntity("http://localhost:8087/settlement/batch", request, ApiResponseDto.class);
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

}
