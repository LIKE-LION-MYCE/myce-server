package com.myce.reservation.service.mapper;

import com.myce.reservation.dto.ExpoAdminEmailRequest;
import com.myce.system.document.EmailLog;
import org.springframework.stereotype.Component;

@Component
public class ExpoAdminEmailMapper {
    public EmailLog toDocument(Long expoId, ExpoAdminEmailRequest dto){
        return EmailLog.builder()
                .expoId(expoId)
                .subject(dto.getSubject())
                .recipientNames(dto.getRecipients())
                .recipientCount(dto.getRecipients().size())
                .content(dto.getContent())
                .build();
    }
}
