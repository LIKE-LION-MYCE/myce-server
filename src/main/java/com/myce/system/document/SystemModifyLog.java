package com.myce.system.document;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@NoArgsConstructor
@Document(collection = "system_modify_logs")
public class SystemModifyLog {

    @Id
    private String id;
    private Long memberId;            // 변경자 ID
    private String memberLoginId;     // 로그인 ID
    private String modifyOption;      // 변경 항목 (예: "권한 변경", "설정 수정")
    private String memberAgent;       // 브라우저/디바이스 정보
    private LocalDateTime createdAt; // 변경 시각

    @Builder
    public SystemModifyLog(Long memberId, String memberLoginId, String modifyOption, String memberAgent) {
        this.memberId = memberId;
        this.memberLoginId = memberLoginId;
        this.modifyOption = modifyOption;
        this.memberAgent = memberAgent;
        this.createdAt = LocalDateTime.now();
    }
}
