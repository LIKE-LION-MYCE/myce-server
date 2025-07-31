package com.myce.system.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Document(collection = "email_logs")
public class EmailLog {

    @Id
    private String id;
    private Long expoId;
    private String title;
    private String recipientName;
    private String content;
    private LocalDateTime createdAt;

    @Builder
    public EmailLog(Long expoId, String title, String recipientName, String content) {
        this.expoId = expoId;
        this.title = title;
        this.recipientName = recipientName;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
