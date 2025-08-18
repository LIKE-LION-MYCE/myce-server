package com.myce.schedule.jobs;

import com.myce.notification.service.EventNotificationService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventNotificationScheduler implements TaskScheduler {

    private final EventNotificationService eventNotificationService;

    @Value("${scheduler.event-notification:0 0 * * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("[Scheduler] 이벤트 1시간 전 알림 스케줄러 초기화, cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.event-notification}")
    public void run() {
        log.info("[Scheduler] 이벤트 1시간 전 알림 스케줄러 실행");
        try {
            process();
        } catch (Exception e) {
            log.error("[Scheduler] 이벤트 1시간 전 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Override
    public void process() {
        eventNotificationService.sendUpcomingEventNotifications();
    }
}
