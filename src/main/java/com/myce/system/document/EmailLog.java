package com.myce.system.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "email_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    private String id;

    private Long expoId;

    private String title;

    private String recipientName;

    private String content;

    private LocalDateTime createdAt;
}
