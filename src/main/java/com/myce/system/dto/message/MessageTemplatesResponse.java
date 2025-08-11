package com.myce.system.dto.message;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class MessageTemplatesResponse {
    int currentPage;
    int totalPage;
    List<MessageTemplateResponse> templates;

    public MessageTemplatesResponse(int currentPage, int totalPage) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.templates = new ArrayList<>();
    }

    public void addMessageTemplate(MessageTemplateResponse template) {
        this.templates.add(template);
    }
}
