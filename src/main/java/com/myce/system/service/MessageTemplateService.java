package com.myce.system.service;

import com.myce.system.dto.MessageTemplate;

public interface MessageTemplateService {
    MessageTemplate getMessageForVerifyingEmail(String code, String limitTime);
}
