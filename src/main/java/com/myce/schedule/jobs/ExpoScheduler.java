package com.myce.schedule.jobs;

import com.myce.expo.service.SystemExpoService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoScheduler implements TaskScheduler {
    
    private final SystemExpoService systemExpoService;

    @Value("${scheduler.expo-publish}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("Expo publishing status scheduler has been registered. cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.expo-publish}")
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            log.error("Error occurred during expo publishing status scheduler execution", e);
        }
    }

    @Override
    @Transactional
    public void process() {
        log.debug("Expo publishing status management process started");
        
        int published = systemExpoService.publishPendingExpos();
        int completed = systemExpoService.closeCompletedExpos();

        if (published > 0 || completed > 0) {
            systemExpoService.refreshExpoCache();
            log.info("Expo status update completed - Published: {}, Ended: {}", published, completed);
            
            // 상세 로그 추가
            if (published > 0) {
                log.info("Scheduler: {} expos transitioned to PUBLISHED status", published);
            }
            if (completed > 0) {
                log.info("Scheduler: {} expos transitioned to PUBLISH_ENDED status", completed);
            }
        } else {
            log.debug("No expos to change status");
        }
        
        log.debug("Expo publishing status management process completed");
    }
}