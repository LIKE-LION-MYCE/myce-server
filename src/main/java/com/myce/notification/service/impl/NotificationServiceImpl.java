package com.myce.notification.service.impl;

import com.myce.notification.document.Notification;
import com.myce.notification.repository.NotificationRepository;
import com.myce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public void saveNotification(Long memberId, Long expoId, String title, String content) {
        try {
            Notification notification = Notification.builder()
                    .memberId(memberId)
                    .expoId(expoId)
                    .title(title)
                    .content(content)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("알림 저장 완료 - 회원 ID: {}, 제목: {}", memberId, title);
        } catch (Exception e) {
            log.error("알림 저장 실패 - 회원 ID: {}, 오류: {}", memberId, e.getMessage(), e);
        }
    }

    @Override
    public void saveQrIssuedNotification(Long memberId, Long expoId, String expoTitle, boolean isReissue) {
        String title = String.format("%s QR코드 %s", expoTitle, isReissue ? "재발급" : "발급");
        String content = isReissue ? 
                "QR코드가 재발급되었습니다." : 
                "QR코드가 발급되었습니다. 박람회 입장 시 사용하세요!";
        
        saveNotification(memberId, expoId, title, content);
    }
}