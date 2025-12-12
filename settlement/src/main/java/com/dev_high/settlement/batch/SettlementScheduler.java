package com.dev_high.settlement.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class SettlementScheduler {

  private final JobLauncher jobLauncher;
  private final Job registrationJob;
  private final Job settlementJob;

  /**
   * 매일 n시 n분 n초 정산 수집 JOB 테스트 중이라
   *
   * @테스트 5분마다 수집
   */
//  @Scheduled(cron = "2 2 2 * * *")
  @Scheduled(cron = "1/5 */5 * * * *")
  public void runRegisterJob() {
    try {
      jobLauncher.run(registrationJob, new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())

          .toJobParameters());

    } catch (JobExecutionException e) {
      log.error("job error: {}", e);
    }
  }

  /**
   * 매달 n일 n시 n분 에 정산시도
   *
   * @테스트 10분마다 시도
   */
//  @Scheduled(cron = "0 5 9 15 * *")
  @Scheduled(cron = "*/10 */10 * * * *")
  public void runSettlementJob() {
    try {
      jobLauncher.run(settlementJob, new JobParametersBuilder().addLong("time",
              System.currentTimeMillis())
          .addString("status", "WAITING")
          .toJobParameters());

    } catch (JobExecutionException e) {
      log.error("job error: {}", e);
    }
  }

  /**
   * @테스트 15분마다 실패 정산 재시도
   */
  //  @Scheduled(cron = "0 30 11 * * *")
  @Scheduled(cron = "* */15 * * * *")
  public void runFailSattlementJob() {
    try {
      jobLauncher.run(settlementJob, new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .addString("status", "FAILED")
          .toJobParameters());

    } catch (JobExecutionException e) {
      log.error("job error: {}", e);
    }
  }

}
