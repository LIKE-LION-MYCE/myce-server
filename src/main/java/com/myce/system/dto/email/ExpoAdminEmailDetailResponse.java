package com.myce.system.dto.email;

import com.myce.system.document.EmailLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExpoAdminEmailDetailResponse {
    private String id;
    private String subject;
    private String content;
    private Integer recipientCount;
    private List<EmailLog.RecipientInfo> recipientInfos;
    private LocalDateTime createdAt;
}
