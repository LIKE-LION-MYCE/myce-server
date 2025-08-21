package com.myce.schedule.jobs;

import com.myce.notification.service.NotificationService;
import com.myce.expo.entity.Event;
import com.myce.expo.repository.EventRepository;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventNotificationScheduler implements TaskScheduler {

    private final NotificationService notificationService;
    private final EventRepository eventRepository;

    @Value("${scheduler.event-notification:0 0,30 * * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("[Scheduler] 이벤트 1시간 전 알림 스케줄러 초기화, cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.event-notification}")
    @Transactional
    public void run() {
        log.info("[Scheduler] 이벤트 1시간 전 알림 스케줄러 실행");
        try {
            process();
        } catch (Exception e) {
            log.error("[Scheduler] 이벤트 1시간 전 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Override
    @Transactional
    public void process() {
        // 1시간 후 시작하는 이벤트들 조회
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        LocalDate targetDate = oneHourLater.toLocalDate();
        LocalTime startTime = oneHourLater.toLocalTime().minusMinutes(30);
        LocalTime endTime = oneHourLater.toLocalTime().plusMinutes(30);

        List<Event> upcomingEvents = eventRepository.findByEventDateAndStartTimeBetween(
                targetDate, startTime, endTime);

        if (upcomingEvents.isEmpty()) {
            log.info("[Scheduler] 1시간 후 시작하는 이벤트가 없습니다.");
            return;
        }

        // 박람회별로 그룹핑하여 중복 알림 방지
        Set<Long> processedExpoIds = upcomingEvents.stream()
                .map(event -> event.getExpo().getId())
                .collect(Collectors.toSet());

        // 각 박람회에 대해 한 번씩만 알림 전송
        for (Long expoId : processedExpoIds) {
            // 해당 박람회의 이벤트 정보 수집
            List<Event> expoEvents = upcomingEvents.stream()
                    .filter(event -> event.getExpo().getId().equals(expoId))
                    .collect(Collectors.toList());
            
            if (!expoEvents.isEmpty()) {
                Event firstEvent = expoEvents.get(0);
                String eventNames = expoEvents.stream()
                        .map(Event::getName)
                        .collect(Collectors.joining(", "));
                
                notificationService.sendEventHourReminderNotification(
                    expoId,
                    eventNames,
                    firstEvent.getStartTime().toString()
                );
                log.info("[Scheduler] 행사 1시간 전 알림 전송 완료 - 박람회: {}, 이벤트: {}",
                        firstEvent.getExpo().getTitle(), eventNames);
            }
        }
    }
}
