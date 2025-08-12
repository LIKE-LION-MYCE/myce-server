package com.myce.system.service.mapper;

import com.myce.system.dto.message.MessageTemplateResponse;
import com.myce.system.dto.message.MessageTemplatesResponse;
import com.myce.system.entity.MessageTemplateSetting;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplateMapper {

    public MessageTemplatesResponse toTemplatesResponse(Page<MessageTemplateSetting> templates) {
        MessageTemplatesResponse response =
                new MessageTemplatesResponse(templates.getNumber(), templates.getTotalPages());

        for(MessageTemplateSetting templateSetting : templates) {
            response.addMessageTemplate(toTemplateResponse(templateSetting));
        }

        return response;
    }

    public MessageTemplateResponse toTemplateResponse(MessageTemplateSetting template) {
        return MessageTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .channelType(template.getChannelType().name())
                .subject(template.getSubject())
                .content(template.getContent())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
