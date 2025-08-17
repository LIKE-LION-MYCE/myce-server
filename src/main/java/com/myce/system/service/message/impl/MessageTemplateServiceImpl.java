package com.myce.system.service.message.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.message.MessageTemplateResponse;
import com.myce.system.dto.message.MessageTemplateListResponse;
import com.myce.system.dto.message.UpdateMessageTemplateRequest;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.repository.MessageTemplateSettingRepository;
import com.myce.system.service.mapper.MessageTemplateMapper;
import com.myce.system.service.message.GenerateMessageService;
import com.myce.system.service.message.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateSettingRepository templateSettingRepository;
    private final GenerateMessageService generateMessageService;
    private final MessageTemplateMapper messageTemplateMapper;

    @Override
    public MessageTemplateListResponse getAllMessageTemplates(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<MessageTemplateSetting> templates = keyword.isBlank() ?
                templateSettingRepository.findAll(pageable) :
                templateSettingRepository.findAllByNameContains(keyword, pageable);
        return messageTemplateMapper.toTemplatesResponse(templates);
    }

    @Override
    public MessageTemplateResponse getMessageTemplateById(long id) {
        MessageTemplateSetting templateSetting = templateSettingRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        String template = "";
        if(templateSetting.getChannelType().equals(ChannelType.EMAIL)) {
            template = generateMessageService.getFullMessage(templateSetting, new Context());
        }
        return messageTemplateMapper.toTemplateResponse(templateSetting, template);
    }

    @Override
    public MessageTemplateResponse updateMessageTemplate(long id, UpdateMessageTemplateRequest request) {
        MessageTemplateSetting templateSetting = templateSettingRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        String name = request.getName();
        String subject = request.getSubject();
        String content = request.getContent();
        templateSetting.updateTemplate(name, subject, content);

        templateSettingRepository.save(templateSetting);

        String template = "";
        if(templateSetting.getChannelType().equals(ChannelType.EMAIL)) {
            template = generateMessageService.getFullMessage(templateSetting, new Context());
        }
        return messageTemplateMapper.toTemplateResponse(templateSetting, template);
    }
}
