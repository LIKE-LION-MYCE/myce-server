package com.myce.system.dto.message;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class MessageTemplateListResponse {
    int currentPage;
    int totalPage;
    List<MessageSummaryResponse> templates;

    public MessageTemplateListResponse(int currentPage, int totalPage) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.templates = new ArrayList<>();
    }

    public void addMessageTemplate(MessageSummaryResponse template) {
        this.templates.add(template);
    }
}
