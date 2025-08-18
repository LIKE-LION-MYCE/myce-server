package com.myce.schedule.jobs;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.notification.service.NotificationService;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoNotificationScheduler implements TaskScheduler {

    private final ExpoRepository expoRepository;
    private final NotificationService notificationService;

    @Value("${scheduler.expo-notification:0 0 9 * * *}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("[Scheduler] 박람회 디데이 알림 스케쥴러 초기화, cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.expo-notification}")
    @Transactional
    public void run() {
        log.info("[Scheduler] 박람회 시작 하루 전 알림 스케줄러 실행 - 실행 시간: {}", java.time.LocalDateTime.now());
        try {
            process();
        } catch (Exception e) {
            log.error("[Scheduler] 박람회 시작 하루 전 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Override
    @Transactional
    public void process() {
        // 내일 시작하는 게시된 박람회 조회
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Expo> exposStartingTomorrow = expoRepository.findByStartDateAndStatus(tomorrow, ExpoStatus.PUBLISHED);

        // 예외처리
        if (exposStartingTomorrow.isEmpty()) {
            log.info("[Scheduler] 내일 시작하는 박람회가 없습니다.");
            return;
        }

        // 각 박람회에 대해 시작 알림 전송
        for (Expo expo : exposStartingTomorrow) {
            notificationService.sendExpoStartNotification(expo.getId());
            log.info("[Scheduler] 박람회 시작 알림 전송 완료 - 박람회: {}", expo.getTitle());
        }
    }
}

