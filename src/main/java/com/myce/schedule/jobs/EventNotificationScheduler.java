package com.myce.schedule.jobs;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Event;
import com.myce.expo.repository.EventRepository;
import com.myce.notification.document.Notification;
import com.myce.notification.entity.type.NotificationType;
import com.myce.notification.entity.type.NotificationTargetType;
import com.myce.notification.repository.NotificationRepository;
import com.myce.notification.service.SseService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.schedule.TaskScheduler;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import com.myce.system.repository.MessageTemplateSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventNotificationScheduler implements TaskScheduler {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final MessageTemplateSettingRepository messageTemplateSettingRepository;
    private final SseService sseService;

    @Value("${scheduler.event-notification}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("[Scheduler] 이벤트 당일 알림 스케줄러 초기화, cron: {}", cronExpression);
    }

    @Override
    @Scheduled(cron = "${scheduler.event-notification}")
    @Transactional
    public void run() {
        log.info("[Scheduler] 이벤트 당일 알림 스케줄러 실행");
        try {
            process();
        } catch (Exception e) {
            log.error("[Scheduler] 이벤트 당일 알림 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Override
    @Transactional
    public void process() {
        // 오늘 하루 종일 진행되는 이벤트 가져오기 (오전 9시에 실행되어 당일 이벤트 알림)
        LocalDate today = LocalDate.now();
        LocalTime dayStart = LocalTime.of(0, 0);
        LocalTime dayEnd = LocalTime.of(23, 59, 59);

        List<Event> todayEvents = eventRepository.findByEventDateAndStartTimeBetween(today, dayStart, dayEnd);
        if (todayEvents.isEmpty()) {
            log.info("[Scheduler] 오늘 진행되는 이벤트가 없습니다.");
            return;
        }

        log.info("[Scheduler] 오늘 진행되는 이벤트 {} 개에 대한 알림을 전송합니다.", todayEvents.size());
        sendNotificationsForEvents(todayEvents);
    }

    private void sendNotificationsForEvents(List<Event> events) {
        MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                        MessageTemplateCode.EVENT_REMINDER, ChannelType.NOTIFICATION)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        List<Notification> notificationsToSave = new ArrayList<>();

        for (Event event : events) {
            String content = template.getContent()
                    .replace("{eventName}", event.getName())
                    .replace("{startTime}", event.getStartTime().toString());

            // SSE 알림 일괄 전송
            sseService.notifyToExpoClient(event.getExpo().getId(), content);
            log.info("[Scheduler] 박람회 ID {}의 예약자들에게 '{}' 이벤트 알림 전송", event.getExpo().getId(), content);

            // 해당 박람회 예약자 정보 조회
            List<Reservation> reservations = reservationRepository.findByExpoId(event.getExpo().getId());

            for (Reservation reservation : reservations) {
                // 회원 예약만 알림 발송
                if (reservation.getUserType() == UserType.MEMBER) {
                    Long userId = reservation.getUserId();
                    Notification notification = Notification.builder()
                            .memberId(userId)
                            .type(NotificationType.EVENT_REMINDER)
                            .targetType(NotificationTargetType.EXPO)
                            .targetId(event.getExpo().getId())
                            .title(template.getSubject())
                            .content(content)
                            .isRead(false)
                            .build();
                    notificationsToSave.add(notification);
                }
            }
        }

        if (!notificationsToSave.isEmpty()) {
            notificationRepository.saveAll(notificationsToSave);
            log.info("[Scheduler] 총 {}개의 이벤트 알림을 DB에 저장했습니다.", notificationsToSave.size());
        }
    }
}
