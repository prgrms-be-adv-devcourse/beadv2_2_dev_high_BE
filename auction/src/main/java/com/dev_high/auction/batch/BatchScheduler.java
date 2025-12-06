package com.dev_high.auction.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job startAuctionsJob;
  private final Job endAuctionsJob;

  // 매시간 정각마다 시작 배치 실행
  @Scheduled(cron = "0 0 * * * *") // 초 분 시 일 월 요일
  public void runStartAuctionsJob() {
    try {
      jobLauncher.run(startAuctionsJob, new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters());
    } catch (Exception e) {
      // 배치 실행 실패 로그
      log.error(e.getMessage());
    }
  }

  // 매시간 정각마다 종료 배치 실행
  @Scheduled(cron = "0 0 * * * *")
  public void runEndAuctionsJob() {
    try {
      jobLauncher.run(endAuctionsJob, new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
