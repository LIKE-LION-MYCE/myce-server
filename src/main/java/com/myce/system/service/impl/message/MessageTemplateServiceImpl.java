package com.myce.system.service.impl.message;

import com.myce.system.dto.message.MessageTemplatesResponse;
import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.repository.MessageTemplateSettingRepository;
import com.myce.system.service.mapper.MessageTemplateMapper;
import com.myce.system.service.message.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final int PAGE_SIZE = 10;

    private final MessageTemplateSettingRepository templateSettingRepository;
    private final MessageTemplateMapper messageTemplateMapper;

    @Override
    public MessageTemplatesResponse getAllMessageTemplates(int page, String keyword) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Page<MessageTemplateSetting> templates = keyword.isBlank() ?
                templateSettingRepository.findAll(pageable) :
                templateSettingRepository.findAllByNameContains(keyword, pageable);
        return messageTemplateMapper.toTemplatesResponse(templates);
    }
}
