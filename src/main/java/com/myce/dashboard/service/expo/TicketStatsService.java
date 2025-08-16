package com.myce.dashboard.service.expo;

import com.myce.dashboard.dto.expo.TicketSales;
import java.util.List;

public interface TicketStatsService {
    
    /**
     * 특정 박람회의 티켓 판매 상세 통계를 조회합니다.
     */
    List<TicketSales> getTicketSalesDetail(Long expoId);
    
    /**
     * 티켓 통계 캐시를 갱신합니다.
     */
    void refreshTicketCache(Long expoId);
    
    /**
     * 티켓 통계 캐시를 완전히 삭제합니다.
     */
    void clearTicketCache(Long expoId);
}