package com.myce.schedule.jobs;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.ExpoRepository;
import com.myce.notification.document.Notification;
import com.myce.notification.repository.NotificationRepository;
import com.myce.notification.service.SseService;
import com.myce.reservation.entity.Reservation;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoNotificationScheduler implements TaskScheduler {

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final MessageTemplateSettingRepository messageTemplateSettingRepository;
    private final SseService sseService;

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
        log.info("[Scheduler] 박람회 시작 하루 전 알림 스케줄러 실행");
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

        // 알림 전송 및 저장
        sendNotificationsForExpos(exposStartingTomorrow);
    }

    // 알림 전송 저장
    private void sendNotificationsForExpos(List<Expo> expos) {
        // 메세지 템플릿 조회
        MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                        MessageTemplateCode.EXPO_REMINDER, ChannelType.NOTIFICATION)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        // 각 박람회에 대한 모든 예약자 매핑
        List<Reservation> allReservations = reservationRepository.findByExpoIn(expos);
        Map<Expo, List<Reservation>> reservationsByExpo = allReservations.stream()
                .collect(Collectors.groupingBy(Reservation::getExpo));

        // mongodb에 저장할 알림 리스트
        List<Notification> notificationsToSave = new ArrayList<>();

        // 내일 시작하는 모든 게시중 박람회에 대해 알림 전송
        for (Expo expo : expos) {
            // 발송할 알림 메세지 작성
            String content = template.getContent().replace("{expoTitle}", expo.getTitle());

            // 해당 박람회에 대한 알림 일괄 전송
            sseService.notifyToExpoClient(expo.getId(), content);

            // 해당 박람회에 대한 예약자 정보 조회
            List<Reservation> reservations = reservationsByExpo.getOrDefault(expo, List.of());

            // 알림 객체 생성
            for (Reservation reservation : reservations) {
                Long userId = reservation.getUserId();
                Notification notification = Notification.builder()
                        .memberId(userId)
                        .expoId(expo.getId())
                        .title(template.getSubject())
                        .content(content)
                        .isRead(false)
                        .build();
                notificationsToSave.add(notification);
                log.info("[Scheduler] 박람회 예약자 {}에게 '{}' 알림 전송 준비 완료", userId, content);
            }
        }

        // 알림 객체 일괄 저장
        if (!notificationsToSave.isEmpty()) {
            notificationRepository.saveAll(notificationsToSave);
            log.info("[Scheduler] 총 {}개의 알림을 DB에 저장했습니다.", notificationsToSave.size());
        }
    }
}

