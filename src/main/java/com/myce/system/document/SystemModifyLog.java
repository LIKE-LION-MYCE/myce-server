package com.myce.system.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "system_modify_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemModifyLog {

    @Id
    private String id;

    private Long memberId;            // 변경자 ID
    private String memberLoginId;     // 로그인 ID
    private String modifyOption;      // 변경 항목 (예: "권한 변경", "설정 수정")
    private String memberAgent;       // 브라우저/디바이스 정보
    private LocalDateTime modifiedAt; // 변경 시각

}
