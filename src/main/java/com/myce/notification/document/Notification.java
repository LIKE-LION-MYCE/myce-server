package com.myce.notification.document;

import com.myce.notification.entity.type.NotificationType;
import com.myce.notification.entity.type.NotificationTargetType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String notificationId;
    private Long memberId;
    private NotificationType type;          // 알림 종류 (EXPO_REMINDER, QR_ISSUED 등)
    private NotificationTargetType targetType;  // 클릭 시 이동할 페이지 타입
    private Long targetId;                  // 이동할 페이지의 ID (expoId, reservationId 등)
    private String title;
    private String content;
    private LocalDateTime readAt;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long memberId, NotificationType type, NotificationTargetType targetType, 
                        Long targetId, String title, String content, LocalDateTime readAt, Boolean isRead) {
        this.memberId = memberId;
        this.type = type;
        this.targetType = targetType;
        this.targetId = targetId;
        this.title = title;
        this.content = content;
        this.readAt = readAt;
        this.isRead = isRead;
        this.createdAt = LocalDateTime.now();
    }
}
