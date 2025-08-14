package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExpoTicketsResponse {
    private Long expoId;
    private String expoTitle;
    private Integer totalTickets;
    private Integer remainingTickets;
    private List<TicketInfo> tickets;
    
    @Getter
    @Builder
    public static class TicketInfo {
        private Long ticketId;
        private String name;
        private String type; // EARLY_BIRD, REGULAR
        private Integer price;
        private Integer totalQuantity;
        private Integer remainingQuantity;
        private String description;
        private Boolean isAvailable; // 구매 가능 여부
    }
}