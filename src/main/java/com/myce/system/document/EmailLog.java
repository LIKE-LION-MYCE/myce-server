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
    private List<String> recipientNames;
    private Integer recipientCount;
    private String content;
    private LocalDateTime createdAt;

    @Builder
    public EmailLog(Long expoId, String subject, List<String> recipientNames, Integer recipientCount, String content) {
        this.expoId = expoId;
        this.subject = subject;
        this.recipientCount = recipientCount;
        this.recipientNames = recipientNames;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}
