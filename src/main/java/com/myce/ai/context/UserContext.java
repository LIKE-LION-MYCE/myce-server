package com.myce.ai.context;

import java.util.List;

/**
 * 사용자별 컨텍스트 정보
 */
public record UserContext(
        String userName,
        String membershipLevel,
        List<String> recentReservations,
        String paymentStatus,
        Long userId
) {}
