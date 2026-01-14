package com.dev_high.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job auctionLifecycleJob;
    private final RedisConnectionFactory redisConnectionFactory;

    // 기본은 10분 간격, 운영은 config 값으로 조정
    @Scheduled(cron = "${auction.batch.lifecycle-cron:0 */10 * * * *}") // 초 분 시 일 월 요일
    public void runAuctionLifecycleJob() {
        try {
            jobLauncher.run(
                    auctionLifecycleJob, new JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters());
        } catch (Exception e) {
            log.warn("auction 상태 변경 scheduled 실패 {}", e.getMessage());
        }
    }

    @Scheduled(cron = "${auction.batch.ranking-clear-cron:0 0 0 * * *}")
    public void clearTodayRanking() {
        try (var connection = redisConnectionFactory.getConnection()) {
            connection.flushDb();
            log.info("auction ranking redis db flushed at midnight");
        } catch (Exception e) {
            log.warn("auction ranking redis flush failed {}", e.getMessage());
        }
    }

}
