package com.myce.system.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@NoArgsConstructor
@Document(collection = "email_logs")
public class EmailLog {

    @Id
    private String id;
    private Long expoId;
    private String subject;
    private List<RecipientInfo> recipientInfos;
    private Integer recipientCount;
    private String content;
    private LocalDateTime createdAt;

    @Builder
    public EmailLog(Long expoId, String subject, List<RecipientInfo> recipientInfos, Integer recipientCount, String content) {
        this.expoId = expoId;
        this.subject = subject;
        this.recipientInfos = recipientInfos;
        this.recipientCount = recipientCount;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        private String email;
        private String name;
    }
}
