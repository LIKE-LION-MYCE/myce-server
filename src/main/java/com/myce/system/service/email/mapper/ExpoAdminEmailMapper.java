package com.myce.system.service.email.mapper;

import com.myce.system.dto.email.ExpoAdminEmailDetailResponse;
import com.myce.system.dto.email.ExpoAdminEmailRequest;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import com.myce.system.document.EmailLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpoAdminEmailMapper {

    private static final int SUBJECT_LIMIT = 50;

    public EmailLog toDocument(Long expoId, ExpoAdminEmailRequest dto, List<EmailLog.RecipientInfo> recipientInfos){
        return EmailLog.builder()
                .expoId(expoId)
                .subject(dto.getSubject())
                .recipientInfos(recipientInfos)
                .recipientCount(recipientInfos.size())
                .content(dto.getContent())
                .build();
    }

    public ExpoAdminEmailResponse toDto(EmailLog document){
        return ExpoAdminEmailResponse.builder()
                .id(document.getId())
                .subject(document.getSubject())
                .content(summarize(document.getContent()))
                .recipientCount(document.getRecipientCount())
                .createdAt(document.getCreatedAt())
                .build();
    }

    public ExpoAdminEmailDetailResponse toDetailDto(EmailLog document){
        return ExpoAdminEmailDetailResponse.builder()
                .id(document.getId())
                .subject(document.getSubject())
                .content(document.getContent())
                .recipientCount(document.getRecipientCount())
                .recipientInfos(document.getRecipientInfos())
                .createdAt(document.getCreatedAt())
                .build();
    }

    private static String summarize(String original) {
        if (original == null){
            return null;
        }

        String stripped = original.strip();

        if (stripped.codePointCount(0, original.length()) <= SUBJECT_LIMIT) {
            return original;
        }

        int cutOffIndex = stripped.offsetByCodePoints(0, SUBJECT_LIMIT);

        return stripped.substring(0, cutOffIndex) + "...";
    }
}
