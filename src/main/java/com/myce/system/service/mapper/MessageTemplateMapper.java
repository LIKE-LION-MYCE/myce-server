package com.myce.system.service.mapper;

import com.myce.system.dto.message.MessageSummaryResponse;
import com.myce.system.dto.message.MessageTemplateResponse;
import com.myce.system.dto.message.MessageTemplateListResponse;
import com.myce.system.entity.MessageTemplateSetting;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplateMapper {

    public MessageTemplateListResponse toTemplatesResponse(Page<MessageTemplateSetting> templates) {
        int currentPage = templates.getNumber() + 1;
        int totalPages = templates.getTotalPages();
        MessageTemplateListResponse response = new MessageTemplateListResponse(currentPage, totalPages);

        for(MessageTemplateSetting templateSetting : templates) {
            response.addMessageTemplate(toSummaryResponse(templateSetting));
        }

        return response;
    }

    private MessageSummaryResponse toSummaryResponse(MessageTemplateSetting template) {
        return MessageSummaryResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .channelType(template.getChannelType().name())
                .subject(template.getSubject())
                .useImage(template.isUseImage())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    public MessageTemplateResponse toTemplateResponse(MessageTemplateSetting setting, String template) {
        return MessageTemplateResponse.builder()
                .id(setting.getId())
                .name(setting.getName())
                .channelType(setting.getChannelType().name())
                .subject(setting.getSubject())
                .template(template)
                .content(setting.getContent())
                .useImage(setting.isUseImage())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
