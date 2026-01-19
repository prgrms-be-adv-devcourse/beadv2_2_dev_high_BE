package com.dev_high.user.seller.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job sellerApproveJob;

    @Scheduled(cron = "${seller.batch.approve-cron:0 */10 * * * *}", zone = "Asia/Seoul")
    public void runHourly() {
        try {
            jobLauncher.run(
                    sellerApproveJob,
                    new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters()
            );
        } catch (Exception e) {
            log.warn("sellerApprove scheduled 실패 {}", e.getMessage());
        }
    }
}
