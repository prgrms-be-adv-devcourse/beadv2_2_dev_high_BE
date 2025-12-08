package com.dev_high.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Slf4j
@Configuration
@EnableScheduling // ⭐ 스케줄링 기능 활성화
@RequiredArgsConstructor
public class OrderScheduleConfig {

    private final JobLauncher jobLauncher;
    private final Job sendConfirmedOrder; // BatchConfig에서 정의한 Job 빈 주입

    /**
     * 매일 새벽 1시 1분 1초에 정산 Job을 실행합니다. (Cron 표현식: 초 분 시 일 월 요일)
     */
    @Scheduled(cron = "1 1 1 * * *")
    public void runSettlementJob() {
        try {
            // Job 재실행을 위해 현재 시간을 파라미터로 추가하여 고유한 JobParameters를 생성합니다.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("runDate", new Date())
                    .toJobParameters();

            // JobLauncher를 사용하여 Job 실행
            jobLauncher.run(sendConfirmedOrder, jobParameters);

        } catch (Exception e) {
            log.error("Failed to start settlementJob via scheduler", e);
        }
    }
}