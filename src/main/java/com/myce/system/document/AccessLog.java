package com.myce.system.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    private String id; // MongoDB의 기본 ID

    private String memberType;       // 관리자, 사용자 등
    private Long memberId;           // 접속한 사용자 ID
    private String memberLoginId;    // 로그인 ID
    private String memberAgent;      // 브라우저/디바이스 정보
    private LocalDateTime accessedAt; // 접속 시각
}

