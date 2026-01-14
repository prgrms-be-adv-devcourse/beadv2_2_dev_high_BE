package com.dev_high.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
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
  private final Job settlementJob;
  private final Job orderStatusJob;

  /**
   * 정산 배치 실행 (기본값은 테스트용, 운영은 config 값 사용)
   */
  @Scheduled(cron = "${settlement.batch.settlement-cron:10 */5 * * * *}" ,scheduler = "settleScheduler")

    public void runSettlementJob() {
    try {
      // WAITING 상태 정산 대상 처리(등록 스텝 포함)
      jobLauncher.run(settlementJob, new JobParametersBuilder().addLong("time",
              System.currentTimeMillis())
          .addString("status", "WAITING")
          .toJobParameters());

    } catch (JobExecutionException e) {
      log.error("job error: {}", e.getMessage(), e);
    }
  }

  /**
   * 실패 정산 재시도 (기본값은 테스트용, 운영은 config 값 사용)
   */
  @Scheduled(cron = "${settlement.batch.retry-cron:15 */6 * * * *}" ,scheduler = "settleScheduler")
  public void runFailSattlementJob() {
    try {
      // FAILED 재시도용 정산 처리(등록 스텝은 reader에서 no-op)
      jobLauncher.run(settlementJob, new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .addString("status", "FAILED")
          .toJobParameters());

    } catch (JobExecutionException e) {
      log.error("job error: {}", e.getMessage(), e);
    }
  }



    /**
     * 주문 상태 전환 (기본값은 테스트용, 운영은 config 값 사용)
     */
    @Scheduled(cron = "${settlement.batch.order-status-cron:5 */3 * * * *}")
    public void runOrderStatusJob() {
        try {
            // 주문 상태 자동 전환 배치 실행
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
