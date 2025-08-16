package com.myce.system.service.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import com.myce.system.repository.MessageTemplateSettingRepository;
import com.myce.system.service.message.GenerateMessageService;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class GenerateMessageServiceImpl implements GenerateMessageService {

    private final MessageTemplateSettingRepository messageTemplateSettingRepository;
    private final SpringTemplateEngine templateEngine;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public MessageTemplate getMessageForVerification
            (String verificationName, String code, String limitTime) {
        MessageTemplateSetting messageTemplate = messageTemplateSettingRepository
                .findByCodeAndChannelType(MessageTemplateCode.EMAIL_VERIFICATION, ChannelType.EMAIL)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("limitTime", limitTime);

        String message = getFullMessage(messageTemplate, context);
        return new MessageTemplate(messageTemplate.getSubject(), message);
    }

    @Override
    public MessageTemplate getMessageForResetPassword(String password) {
        MessageTemplateSetting messageTemplate = messageTemplateSettingRepository
                .findByCodeAndChannelType(MessageTemplateCode.RESET_PASSWORD, ChannelType.EMAIL)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        String content = messageTemplate.getContent();
        content = content.replace("{{TEMP_PASSWORD}}", password);

        return new MessageTemplate(messageTemplate.getSubject(), content);
    }

    private String getFullMessage(MessageTemplateSetting messageTemplate, Context context) {
        Map<String, String> templateData = parseJsonContent(messageTemplate.getContent());

        for (Map.Entry<String, String> entry : templateData.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        return templateEngine.process("mail/mail-code",context);
    }


    private Map<String, String> parseJsonContent(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, Map.class);
        } catch (JsonProcessingException je) {
            throw new IllegalArgumentException("JSON 파싱 실패: " + je.getMessage(), je);
        }
    }
}
