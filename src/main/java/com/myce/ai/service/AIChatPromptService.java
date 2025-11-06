package com.myce.ai.service;


import com.myce.ai.context.PublicContext;
import com.myce.ai.context.UserContext;

public interface AIChatPromptService {
    String createSystemPromptWithContext(UserContext userContext, PublicContext publicContext, boolean isWaitingForAdmin, boolean shouldSuggestHuman);
    String createAIPromptWithHistoryAndUserMessage(String systemPrompt, String conversationHistory, String userMessage);
    String createSummaryPromptWithContextAndLog(UserContext userContext, StringBuilder conversationLog);
}
