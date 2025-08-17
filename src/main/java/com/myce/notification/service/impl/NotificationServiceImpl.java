package com.myce.notification.service.impl;

import com.myce.notification.document.Notification;
import com.myce.notification.dto.NotificationResponse;
import com.myce.notification.entity.type.NotificationType;
import com.myce.notification.entity.type.NotificationTargetType;
import com.myce.notification.repository.NotificationRepository;
import com.myce.notification.service.NotificationService;
import com.myce.notification.service.SseService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import com.myce.system.repository.MessageTemplateSettingRepository;
import com.myce.common.exception.CustomException;
import com.myce.common.exception.CustomErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.myce.system.entity.type.MessageTemplateCode.QR_ISSUED;
import static com.myce.system.entity.type.MessageTemplateCode.QR_REISSUED;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;
    private final ReservationRepository reservationRepository;
    private final MessageTemplateSettingRepository messageTemplateSettingRepository;


    @Override
    public void saveNotification(Long memberId, Long targetId, String title, String content, 
                                NotificationType type, NotificationTargetType targetType) {
        try {
            Notification notification = Notification.builder()
                    .memberId(memberId)
                    .type(type)
                    .targetType(targetType)
                    .targetId(targetId)
                    .title(title)
                    .content(content)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            
            // SSE 실시간 알림 전송
            String message = String.format(
                "{\"type\":\"%s\",\"message\":\"%s\"}",
                type.name(),
                content
            );
            sseService.notifyMemberViaSseEmitters(memberId, message);
            
            log.info("알림 저장 및 SSE 전송 완료 - 회원 ID: {}, 제목: {}, 타입: {}", memberId, title, type);
        } catch (Exception e) {
            log.error("알림 저장 실패 - 회원 ID: {}, 타입: {}, 오류: {}", memberId, type, e.getMessage(), e);
        }
    }

    @Override
    public List<NotificationResponse> getNotificationsByMemberId(Long memberId) {
        try {
            // 최신순으로 정렬하여 조회
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            List<Notification> notifications = notificationRepository.findByMemberId(memberId, sort);

            return notifications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("알림 목록 조회 실패 - 회원 ID: {}, 오류: {}", memberId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void markAsRead(String notificationId, Long memberId) {
        try {
            // MongoDB @Update를 사용하여 원자적 업데이트 수행
            // Query 조건에 memberId도 포함하여 권한 체크와 업데이트를 한번에 처리
            notificationRepository.markAsRead(notificationId, memberId, LocalDateTime.now());
            log.info("알림 읽음 처리 완료 - 알림 ID: {}, 회원 ID: {}", notificationId, memberId);
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패 - 알림 ID: {}, 회원 ID: {}, 오류: {}", notificationId, memberId, e.getMessage(), e);
        }
    }

    @Override
    public void markAllAsRead(Long memberId) {
        try {
            // 해당 회원의 모든 읽지 않은 알림을 읽음 처리
            notificationRepository.markAllAsReadByMemberId(memberId, LocalDateTime.now());
            log.info("모든 알림 읽음 처리 완료 - 회원 ID: {}", memberId);
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패 - 회원 ID: {}, 오류: {}", memberId, e.getMessage(), e);
        }
    }

    @Override
    public void sendQrIssuedNotification(Long memberId, Long reservationId, String expoTitle, boolean isReissue) {
        try {
            MessageTemplateCode templateCode = isReissue ? QR_REISSUED : QR_ISSUED;
            MessageTemplateSetting template = messageTemplateSettingRepository
                    .findByCodeAndChannelType(templateCode, ChannelType.NOTIFICATION)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

            String title = template.getSubject();
            String content = template.getContent().replace("{expoTitle}", expoTitle);
            
            // 공통 saveNotification 메서드 호출
            saveNotification(memberId, reservationId, title, content, 
                           NotificationType.QR_ISSUED, NotificationTargetType.RESERVATION);
                           
            log.info("QR 발급 알림 처리 완료 - 회원 ID: {}, 예매 ID: {}", memberId, reservationId);
        } catch (Exception e) {
            log.error("QR 발급 알림 처리 실패 - 회원 ID: {}, 예매 ID: {}, 오류: {}", memberId, reservationId, e.getMessage(), e);
        }
    }

    @Override
    public void sendExpoStartNotification(Long expoId) {
        try {
            // 메시지 템플릿 조회
            MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                            MessageTemplateCode.EXPO_REMINDER, ChannelType.NOTIFICATION)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

            // 해당 박람회 예약자들 조회
            List<Reservation> reservations = reservationRepository.findByExpoId(expoId);
            
            if (reservations.isEmpty()) {
                log.info("알림 전송 대상이 없습니다 - 박람회 ID: {}", expoId);
                return;
            }

            // 박람회 제목 가져오기 (첫 번째 예약에서)
            String expoTitle = reservations.get(0).getExpo().getTitle();
            String content = template.getContent().replace("{expoTitle}", expoTitle);

            // 각 예약자에게 알림 전송
            int notificationCount = 0;
            for (Reservation reservation : reservations) {
                // 회원 예약만 알림 발송
                if (reservation.getUserType() == UserType.MEMBER) {
                    saveNotification(
                        reservation.getUserId(), 
                        expoId, 
                        template.getSubject(), 
                        content,
                        NotificationType.GENERAL,
                        NotificationTargetType.EXPO
                    );
                    notificationCount++;
                }
            }
            
            log.info("박람회 시작 알림 처리 완료 - 박람회 ID: {}, 알림 수: {} 개", 
                    expoId, notificationCount);
                    
        } catch (Exception e) {
            log.error("박람회 시작 알림 전송 실패 - 박람회 ID: {}, 오류: {}", 
                    expoId, e.getMessage(), e);
        }
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}