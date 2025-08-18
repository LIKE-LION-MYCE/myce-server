package com.myce.notification.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Event;
import com.myce.expo.repository.EventRepository;
import com.myce.notification.document.Notification;
import com.myce.notification.entity.type.NotificationType;
import com.myce.notification.entity.type.NotificationTargetType;
import com.myce.notification.repository.NotificationRepository;
import com.myce.notification.service.EventNotificationService;
import com.myce.notification.service.SseService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import com.myce.system.repository.MessageTemplateSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationServiceImpl implements EventNotificationService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final MessageTemplateSettingRepository messageTemplateSettingRepository;
    private final SseService sseService;

    @Override
    @Transactional
    public void sendUpcomingEventNotifications() {
        try {
            // 현재 시간으로부터 1시간 후에 시작하는 이벤트들 조회
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);
            
            // 1시간 후의 날짜와 시간 범위 (± 30분 윈도우)
            LocalDate targetDate = oneHourLater.toLocalDate();
            LocalTime startTime = oneHourLater.toLocalTime().minusMinutes(30);
            LocalTime endTime = oneHourLater.toLocalTime().plusMinutes(30);
            
            List<Event> upcomingEvents = eventRepository.findByEventDateAndStartTimeBetween(
                targetDate, startTime, endTime);
            
            if (upcomingEvents.isEmpty()) {
                log.debug("[EventNotification] 1시간 후 시작하는 이벤트가 없습니다. 현재시간: {}", now);
                return;
            }

            // 이미 알림이 전송된 이벤트들 필터링 (중복 방지)
            List<Event> eventsToNotify = filterAlreadyNotifiedEvents(upcomingEvents);
            
            if (eventsToNotify.isEmpty()) {
                log.debug("[EventNotification] 모든 이벤트에 대해 이미 알림이 전송되었습니다.");
                return;
            }

            log.info("[EventNotification] 1시간 후 시작하는 {} 개 이벤트에 대한 알림을 전송합니다.", eventsToNotify.size());
            sendNotificationsForEvents(eventsToNotify);
            
        } catch (Exception e) {
            log.error("[EventNotification] 이벤트 알림 처리 중 오류 발생", e);
        }
    }

    /**
     * 이미 알림이 전송된 이벤트들을 필터링하여 제외
     */
    private List<Event> filterAlreadyNotifiedEvents(List<Event> events) {
        if (events.isEmpty()) {
            return events;
        }

        // 오늘 전송된 EVENT_REMINDER 알림의 targetId(expoId) 목록 조회
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        
        // MongoDB aggregation을 사용하여 이미 알림이 전송된 expoId들을 조회
        List<Notification> todayEventNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getType() == NotificationType.EVENT_REMINDER)
                .filter(n -> n.getCreatedAt().isAfter(todayStart))
                .collect(Collectors.toList());
        
        Set<Long> notifiedExpoIds = todayEventNotifications.stream()
                .map(Notification::getTargetId)
                .collect(Collectors.toSet());

        // 아직 알림이 전송되지 않은 이벤트들만 필터링
        return events.stream()
                .filter(event -> !notifiedExpoIds.contains(event.getExpo().getId()))
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 목록에 대해 알림 전송
     */
    private void sendNotificationsForEvents(List<Event> events) {
        MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                        MessageTemplateCode.EVENT_REMINDER, ChannelType.NOTIFICATION)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        List<Notification> notificationsToSave = new ArrayList<>();

        for (Event event : events) {
            String content = template.getContent()
                    .replace("{eventName}", event.getName())
                    .replace("{startTime}", event.getStartTime().toString());

            // SSE 실시간 알림 전송 (ClientAbortException 방어)
            try {
                sseService.notifyToExpoClient(event.getExpo().getId(), content);
                log.info("[EventNotification] 박람회 ID {}의 예약자들에게 '{}' 이벤트 1시간 전 알림 전송", 
                        event.getExpo().getId(), event.getName());
            } catch (Exception sseError) {
                log.warn("[EventNotification] SSE 알림 전송 실패 (계속 진행): 박람회 ID {}, 이벤트 '{}', 오류: {}", 
                        event.getExpo().getId(), event.getName(), sseError.getMessage());
            }

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
                            .targetId(event.getExpo().getId()) // 박람회 ID로 설정 (박람회 상세 페이지로 이동)
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
            log.info("[EventNotification] 총 {}개의 이벤트 1시간 전 알림을 DB에 저장했습니다.", notificationsToSave.size());
        }
    }
}