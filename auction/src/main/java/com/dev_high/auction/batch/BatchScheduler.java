package com.dev_high.auction.batch;

import com.dev_high.auction.kafka.AuctionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
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
      log.error("StartAuctionsJob 실행 중 오류 발생", e);
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
      log.error("endAuctionsJob 실행 중 예외 발생", e);

    }
  }
}
