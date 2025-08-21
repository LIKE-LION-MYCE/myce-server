package com.myce.system.service.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import com.myce.reservation.entity.code.UserType;
import com.myce.system.repository.MessageTemplateSettingRepository;
import com.myce.system.service.message.GenerateMessageService;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
        context.setVariable("verificationName", verificationName);
        context.setVariable("limitTime", limitTime);

        String message = getFullMessage(messageTemplate, context);
        return new MessageTemplate(messageTemplate.getSubject(), message);
    }

    @Override
    public MessageTemplate getMessageForResetPassword(String password) {
        MessageTemplateSetting messageTemplate = messageTemplateSettingRepository
                .findByCodeAndChannelType(MessageTemplateCode.RESET_PASSWORD, ChannelType.EMAIL)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        Context context = new Context();
        context.setVariable("tempPassword", password);

        String message = getFullMessage(messageTemplate, context);
        return new MessageTemplate(messageTemplate.getSubject(), message);
    }

    @Override
    public MessageTemplate getMessageForReservationConfirmation(String name, String expoTitle, 
            String reservationCode, Integer quantity, String paymentAmount, UserType userType) {
        MessageTemplateSetting messageTemplate = messageTemplateSettingRepository
                .findByCodeAndChannelType(MessageTemplateCode.RESERVATION_CONFIRM, ChannelType.EMAIL)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MESSAGE_TEMPLATE));

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("expoTitle", expoTitle);
        context.setVariable("reservationCode", reservationCode);
        context.setVariable("quantity", quantity);
        context.setVariable("paymentAmount", paymentAmount);
        context.setVariable("userType", userType);
        
        // 회원/비회원에 따른 안내 문구 설정
        String securityContent;
        if (userType == UserType.MEMBER) {
            securityContent = "• 예매 확인 및 QR 코드는 <a href='https://www.myce.live'>MYCE</a> 로그인 후, 마이페이지에서 확인 가능합니다<br>" +
                            "• 박람회 당일 QR 코드를 제시해주세요<br>" +
                            "• 문의사항이 있으시면 고객센터로 연락해주세요";
        } else {
            securityContent = "• 예매 확인 및 QR 코드는 <a href='https://www.myce.live/guest-reservation'>비회원 예매 확인</a>에서 확인 가능합니다<br>" +
                            "• 박람회 당일 QR 코드를 제시해주세요<br>" +
                            "• 문의사항이 있으시면 고객센터로 연락해주세요";
        }
        context.setVariable("securityContent", securityContent);

        String message = getFullMessage(messageTemplate, context);
        return new MessageTemplate(messageTemplate.getSubject(), message);
    }

    public String getFullMessage(MessageTemplateSetting messageTemplate, Context context) {
        Map<String, String> templateData = parseJsonContent(messageTemplate.getContent());

        // JSON 데이터를 먼저 설정하고, 동적 데이터가 덮어쓰도록 함
        for (Map.Entry<String, String> entry : templateData.entrySet()) {
            if (!context.containsVariable(entry.getKey())) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }

        String target = getTargetFile(messageTemplate.getCode(), messageTemplate.isUseImage());
        return templateEngine.process(target,context);
    }

    private String getTargetFile(MessageTemplateCode code, boolean isUseImage) {
        if(code.equals(MessageTemplateCode.RESET_PASSWORD)) {
            return "mail/mail-password";
        }
        
        if(code.equals(MessageTemplateCode.RESERVATION_CONFIRM)) {
            return "mail/mail-reservation";
        }

        return isUseImage ? "mail/mail-image" : "mail/mail-code";
    }

    private Map<String, String> parseJsonContent(String jsonContent) {
        try {
            return objectMapper.readValue(jsonContent, Map.class);
        } catch (JsonProcessingException je) {
            throw new IllegalArgumentException("JSON 파싱 실패: " + je.getMessage(), je);
        }
    }
}
