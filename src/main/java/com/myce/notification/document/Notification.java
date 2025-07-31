package com.myce.notification.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
