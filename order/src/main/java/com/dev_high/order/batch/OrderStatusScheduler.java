package com.dev_high.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class OrderStatusScheduler {


  private final JobLauncher jobLauncher;
  private final Job orderStatusJob;

  /**
   * 매일 새벽 n시 n분에 실행
   */
//  @Scheduled(cron = "0 11 1 * * *")
  @Scheduled(cron = "3 1/3 * * * *")
  public void runOrderStatusJob() {
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis()) // 매 실행마다 다른 파라미터
          .toJobParameters();

      jobLauncher.run(orderStatusJob, params);
      log.info("OrderStatusJob 실행 완료");
    } catch (Exception e) {
      log.error("OrderStatusJob 실행 중 오류", e);
    }
  }
}
