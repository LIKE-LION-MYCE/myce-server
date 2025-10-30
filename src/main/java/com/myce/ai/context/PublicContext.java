package com.myce.ai.context;

import java.util.List;

/**
 * 공개 플랫폼 정보
 */
public record PublicContext(
        List<String> availableExpos,
        String platformInfo,
        String pricingInfo
) {}