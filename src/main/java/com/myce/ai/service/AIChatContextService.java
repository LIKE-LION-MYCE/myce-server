package com.myce.ai.service;


import com.myce.ai.context.PublicContext;
import com.myce.ai.context.UserContext;

public interface AIChatContextService {
    UserContext buildUserContext(String roomCode);
    PublicContext buildPublicContext();
}
