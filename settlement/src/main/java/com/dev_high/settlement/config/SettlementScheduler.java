package com.dev_high.settlement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;

    private final Job registrationJob;

    private final Job processingJob;

//    private final Job retryJob;
//
//    private final Job notificationJob;

    /**
     * 매일 2시 2분 2초 정산 수집 JOB
     */
    @Scheduled(cron = "1/30 * * * * *")
    public void runRegisterJob() {
        try {
            runJob(registrationJob, "registrationJob");
        } catch (JobExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 매달 3일 3시 3분 3초 정산 처리 JOB
     */
    @Scheduled(cron = "1/40 * * * * *")
    public void runProcessingJob() {
        try {
            runJob(processingJob, "processingJob");
        } catch (JobExecutionException e) {
            throw new RuntimeException(e);
        }
    }


//    /**
//     * 매일 4시 4분 4초 정산 재시도 JOB
//     */
//    @Scheduled(cron = "4 4 4 * * *")
//    public void runRetryJob() {
//        try {
//            runJob(retryJob, "retryJob");
//        } catch (JobExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    /**
//     * 매일 5시 5분 5초 3회 이상 실패 정산 알림 발행 JOB
//     */
//    @Scheduled(cron = "5 5 5 * * *")
//    public void runNotifyJob() {
//        try {
//            runJob(notificationJob, "notifyJob");
//        } catch (JobExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }


    /**
     * Job 실행 공통 로직
     */
    private void runJob(Job job, String jobName) throws JobExecutionException {
        // JobParameters를 고유하게 만들어 동일한 Job이 재실행되도록 합니다.
        // Spring Batch는 동일한 JobParameters로는 Job을 재실행하지 않습니다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("run.id", jobName + "_" + LocalDateTime.now())
                .toJobParameters();

        log.info("{} start :{}", jobName, LocalDateTime.now());

        // Job 실행
        jobLauncher.run(job, jobParameters);

        log.info("{} complete :{}", jobName, LocalDateTime.now());
    }
}
