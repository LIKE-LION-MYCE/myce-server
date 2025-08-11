package com.myce.system.service.message;

import com.myce.system.dto.message.MessageTemplatesResponse;

public interface MessageTemplateService {

    MessageTemplatesResponse getAllMessageTemplates(int page, String keyword);
}
