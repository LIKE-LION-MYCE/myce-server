package com.myce.system.service.message;

import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.entity.MessageTemplateSetting;
import org.thymeleaf.context.Context;

public interface GenerateMessageService {
    MessageTemplate getMessageForVerification(String verificationName, String code, String limitTime);

    MessageTemplate getMessageForResetPassword(String password);

    String getFullMessage(MessageTemplateSetting messageTemplate, Context context);
}
