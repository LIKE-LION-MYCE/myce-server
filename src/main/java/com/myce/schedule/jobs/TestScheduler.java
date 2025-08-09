package com.myce.schedule.jobs;

import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestScheduler implements TaskScheduler {

    @PostConstruct
    public void init() {
        log.debug("[Scheduler] Registered test scheduler.");
    }

    @Override
    @Scheduled(cron = "${scheduler.test: 0 0 * * * *}") // every hour
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            log.error("Fail to run test scheduler.", e);
        }
    }

    @Override
    public void process() {
        // 스케쥴러 작업 개발
        log.debug("test-task");
    }
}
