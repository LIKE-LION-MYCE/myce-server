package com.myce.notification.document;

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
    private Long expoId;
    private String title;
    private String content;
    private LocalDateTime readAt;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long memberId, Long expoId, String title, String content,
                        LocalDateTime readAt, Boolean isRead) {
        this.memberId = memberId;
        this.expoId = expoId;
        this.title = title;
        this.content = content;
        this.readAt = readAt;
        this.isRead = isRead;
        this.createdAt = LocalDateTime.now();
    }
}
