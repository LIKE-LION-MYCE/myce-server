package com.myce.system.service.message;

import com.myce.system.dto.message.MessageTemplate;
import com.myce.system.dto.message.MessageTemplateResponse;
import com.myce.system.dto.message.MessageTemplateListResponse;
import com.myce.system.dto.message.UpdateMessageTemplateRequest;

public interface MessageTemplateService {

    MessageTemplateListResponse getAllMessageTemplates(int page, String keyword);

    MessageTemplateResponse getMessageTemplateById(long id);

    MessageTemplateResponse updateMessageTemplate(long id, UpdateMessageTemplateRequest request);
}
