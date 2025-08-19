package com.myce.notification.service.impl;

import com.myce.advertisement.entity.Advertisement;
import com.myce.expo.entity.Expo;
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
import java.util.stream.Collectors;

import static com.myce.system.entity.type.MessageTemplateCode.QR_ISSUED;
import static com.myce.system.entity.type.MessageTemplateCode.QR_REISSUED;
import static com.myce.system.entity.type.MessageTemplateCode.PAYMENT_COMPLETE;
import static com.myce.system.entity.type.MessageTemplateCode.EVENT_REMINDER;

import com.myce.expo.repository.ExpoRepository;
import com.myce.advertisement.repository.AdRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;
    private final ReservationRepository reservationRepository;
    private final MessageTemplateSettingRepository messageTemplateSettingRepository;
    private final ExpoRepository expoRepository;
    private final AdRepository advertisementRepository;


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
    @org.springframework.transaction.annotation.Transactional
    public void sendQrIssuedNotificationByReservationId(Long reservationId) {
        try {
            // 새로운 트랜잭션에서 예약 정보를 조회하여 LazyInitializationException 방지
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
            
            // MEMBER 타입인 경우에만 알림 전송
            if (reservation.getUserType() != UserType.MEMBER) {
                log.debug("알림 건너뜀 - 회원이 아닌 예약 (UserType: {}, 예약 ID: {})", 
                        reservation.getUserType(), reservation.getId());
                return;
            }
            
            Long memberId = reservation.getUserId();
            String expoTitle = reservation.getExpo().getTitle();
            
            // 기존 sendQrIssuedNotification 메서드 호출
            sendQrIssuedNotification(memberId, reservation.getId(), expoTitle, false);
            
            log.info("QR 발급 알림 처리 완료 - 예약 ID: {}, 회원 ID: {}", 
                    reservation.getId(), memberId);
        } catch (Exception e) {
            log.error("QR 발급 알림 처리 실패 - 예약 ID: {}, 오류: {}", 
                    reservationId, e.getMessage(), e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    @Override
    public void sendPaymentCompleteNotification(Long memberId, Long reservationId, String expoTitle, String paymentAmount) {
        try {
            MessageTemplateSetting template = messageTemplateSettingRepository
                    .findByCodeAndChannelType(PAYMENT_COMPLETE, ChannelType.NOTIFICATION)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

            String title = template.getSubject();
            String content = template.getContent()
                    .replace("{expoTitle}", expoTitle)
                    .replace("{paymentAmount}", paymentAmount);
            
            // 공통 saveNotification 메서드 호출
            saveNotification(memberId, reservationId, title, content, 
                           NotificationType.RESERVATION_CONFIRM, NotificationTargetType.RESERVATION);

                           
            log.info("결제 완료 알림 처리 완료 - 회원 ID: {}, 예매 ID: {}, 금액: {}", memberId, reservationId, paymentAmount);
        } catch (Exception e) {
            log.error("결제 완료 알림 처리 실패 - 회원 ID: {}, 예매 ID: {}, 오류: {}", memberId, reservationId, e.getMessage(), e);
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
            List<Long> userIds = reservationRepository.findDistinctUserIdsByExpoId(expoId);
            
            if (userIds.isEmpty()) {
                log.info("알림 전송 대상이 없습니다 - 박람회 ID: {}", expoId);
                return;
            }

            // 박람회 정보 조회
            Expo expo = expoRepository.findById(expoId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
            String expoTitle = expo.getTitle();
            String content = template.getContent().replace("{expoTitle}", expoTitle);

            // 각 예약자에게 알림 전송
            for (Long userId : userIds) {
                saveNotification(
                    userId, 
                    expoId, 
                    template.getSubject(), 
                    content,
                    NotificationType.EXPO_REMINDER,
                    NotificationTargetType.EXPO
                );
            }
            int notificationCount = userIds.size();
            
            log.info("박람회 시작 알림 처리 완료 - 박람회 ID: {}, 알림 수: {} 개", 
                    expoId, notificationCount);
                    
        } catch (Exception e) {
            log.error("박람회 시작 알림 전송 실패 - 박람회 ID: {}, 오류: {}", 
                    expoId, e.getMessage(), e);
        }
    }

    @Override
    public void sendEventHourReminderNotification(Long expoId, String eventName, String startTime) {
        try {
            // 메시지 템플릿 조회
            MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                            EVENT_REMINDER, ChannelType.NOTIFICATION)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

            // 해당 박람회 예약자들 조회
            List<Long> userIds = reservationRepository.findDistinctUserIdsByExpoId(expoId);
            
            if (userIds.isEmpty()) {
                log.info("1시간 전 알림 전송 대상이 없습니다 - 박람회 ID: {}", expoId);
                return;
            }

            // 박람회 정보 조회
            Expo expo = expoRepository.findById(expoId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
            String content = template.getContent()
                    .replace("{expoTitle}", expo.getTitle())
                    .replace("{eventName}", eventName)
                    .replace("{startTime}", startTime);

            // 각 예약자에게 알림 전송
            for (Long userId : userIds) {
                saveNotification(
                    userId, 
                    expoId, 
                    template.getSubject(), 
                    content,
                    NotificationType.EVENT_REMINDER,
                    NotificationTargetType.EXPO
                );
            }
            int notificationCount = userIds.size();
            
            log.info("행사 1시간 전 알림 처리 완료 - 박람회 ID: {}, 알림 수: {} 개", 
                    expoId, notificationCount);
                    
        } catch (Exception e) {
            log.error("행사 1시간 전 알림 전송 실패 - 박람회 ID: {}, 오류: {}", 
                    expoId, e.getMessage(), e);
        }
    }

    @Override
    public void sendExpoStatusChangeNotification(Long expoId, String expoTitle, String oldStatus, String newStatus) {
        try {

            MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                            MessageTemplateCode.EXPO_STATUS_CHANGE, ChannelType.NOTIFICATION)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));
            // 박람회 정보 조회
            Expo expo = expoRepository.findById(expoId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

            Long memberId = expo.getMember().getId();


            String title = template.getSubject();
            String content = template.getContent()
                    .replace("{expoTitle}", expoTitle)
                    .replace("{oldStatus}", getStatusDisplayName(oldStatus))
                    .replace("{newStatus}", getStatusDisplayName(newStatus));

            saveNotification(memberId, expoId, title, content,
                    NotificationType.EXPO_STATUS_CHANGE, NotificationTargetType.EXPO_STATUS);


            log.info("박람회 상태 변경 알림 처리 완료 - 박람회 ID: {}, 회원 ID: {}, {} -> {}",
                    expoId, memberId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("박람회 상태 변경 알림 전송 실패 - 박람회 ID: {}, 오류: {}", expoId, e.getMessage(), e);
        }
    }

    @Override
    public void sendAdvertisementStatusChangeNotification(Long advertisementId, String adTitle, String oldStatus, String newStatus) {
        try {

            MessageTemplateSetting template = messageTemplateSettingRepository.findByCodeAndChannelType(
                            MessageTemplateCode.AD_STATUS_CHANGE, ChannelType.NOTIFICATION)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

            Advertisement advertisement = advertisementRepository.findById(advertisementId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));

            Long memberId = advertisement.getMember().getId();

            String title = template.getSubject();
            String content = template.getContent()
                    .replace("{adTitle}", adTitle)
                    .replace("{oldStatus}", getStatusDisplayName(oldStatus))
                    .replace("{newStatus}", getStatusDisplayName(newStatus));

            saveNotification(memberId, advertisementId, title, content,
                    NotificationType.AD_STATUS_CHANGE, NotificationTargetType.AD_STATUS);


            log.info("광고 상태 변경 알림 처리 완료 - 광고 ID: {}, 회원 ID: {}, {} -> {}",
                    advertisementId, memberId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("광고 상태 변경 알림 전송 실패 - 광고 ID: {}, 오류: {}", advertisementId, e.getMessage(), e);
        }
    }
    
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "PENDING_APPROVAL":
                return "승인 대기";
            case "PENDING_PAYMENT":
                return "결제 대기";
            case "PENDING_PUBLISH":
                return "게시 대기";
            case "PENDING_CANCEL":
                return "취소 대기";
            case "PUBLISHED":
                return "게시 중";
            case "PUBLISH_ENDED":
                return "게시 종료";
            case "SETTLEMENT_REQUESTED":
                return "정산 요청";
            case "COMPLETED":
                return "종료됨";
            case "REJECTED":
                return "승인 거절";
            case "CANCELLED":
                return "취소 완료";
            default:
                return status;
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