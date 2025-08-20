package com.myce.schedule.jobs;

import com.myce.advertisement.service.SystemAdService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdScheduler implements TaskScheduler {
    private final SystemAdService systemAdService;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        log.debug("[Scheduler] Registered advertisement scheduler.");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        try {
            log.debug("[Scheduler] ApplicationReadyEvent - initial advertisement processing start.");
            this.process();
            log.debug("[Scheduler] ApplicationReadyEvent - initial advertisement processing done.");
        } catch (Exception e) {
            log.error("Fail to run initial advertisement processing on ApplicationReadyEvent.", e);
        }
    }

    @Override
    @Scheduled(cron = "${scheduler.ad-publish}")
    public void run() {
        try{
            this.process();
        }catch (Exception e){
            log.error("Fail to run advertisement scheduler.", e);
        }
    }

    @Override
    public void process() {
        systemAdService.updateAdStatus();
    }

}
