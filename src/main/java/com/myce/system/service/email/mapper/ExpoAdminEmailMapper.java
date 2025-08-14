package com.myce.system.service.email.mapper;

import com.myce.system.dto.email.ExpoAdminEmailRequest;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import com.myce.system.document.EmailLog;
import org.springframework.stereotype.Component;

@Component
public class ExpoAdminEmailMapper {
    public EmailLog toDocument(Long expoId, ExpoAdminEmailRequest dto){
        return EmailLog.builder()
                .expoId(expoId)
                .subject(dto.getSubject())
                .recipientInfos(dto.getRecipientInfos())
                .content(dto.getContent())
                .build();
    }

    public ExpoAdminEmailResponse toDto(EmailLog document){
        return ExpoAdminEmailResponse.builder()
                .id(document.getId())
                .subject(document.getSubject())
                .content(document.getContent())
                .recipientCount(document.getRecipientCount())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
