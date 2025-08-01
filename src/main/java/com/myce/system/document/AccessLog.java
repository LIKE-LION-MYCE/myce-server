package com.myce.system.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Document(collection = "access_logs")
public class AccessLog {

    @Id
    private String id;
    private String memberType;
    private Long memberId;
    private String memberLoginId;
    private String memberAgent;
    private LocalDateTime createdAt;

    @Builder
    public AccessLog(String memberType, Long memberId, String memberLoginId, String memberAgent) {
        this.memberType = memberType;
        this.memberId = memberId;
        this.memberLoginId = memberLoginId;
        this.memberAgent = memberAgent;
        this.createdAt = LocalDateTime.now();
    }
}

