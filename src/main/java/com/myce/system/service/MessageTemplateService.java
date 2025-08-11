package com.myce.system.service;

import com.myce.system.dto.MessageTemplate;

public interface MessageTemplateService {
    MessageTemplate getMessageForVerification(String verificationName, String code, String limitTime);

    MessageTemplate getMessageForResetPassword(String password);
}
