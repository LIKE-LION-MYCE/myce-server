package com.myce.system.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.MessageTemplate;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import com.myce.system.repository.MessageTemplateSettingRepository;
import com.myce.system.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateSettingRepository messageTemplateSettingRepository;

    @Override
    public MessageTemplate getMessageForVerification
            (String verificationName, String code, String limitTime) {
        MessageTemplateSetting messageTemplate = messageTemplateSettingRepository
                .findByCodeAndChannelType(MessageTemplateCode.EMAIL_VERIFICATION, ChannelType.EMAIL)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        String content = messageTemplate.getContent();
        content = content.replace("{{CODE}}", code);
        content = content.replace("{{VERIFICATION_NAME}}", verificationName);
        content = content.replace("{{LIMIT_TIME}}", limitTime);

        return new MessageTemplate(messageTemplate.getSubject(), content);
    }

}
