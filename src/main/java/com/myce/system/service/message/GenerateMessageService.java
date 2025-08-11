package com.myce.system.service.message;

import com.myce.system.dto.message.MessageTemplate;

public interface GenerateMessageService {
    MessageTemplate getMessageForVerification(String verificationName, String code, String limitTime);

    MessageTemplate getMessageForResetPassword(String password);
}
