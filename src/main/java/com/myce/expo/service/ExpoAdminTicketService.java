package com.myce.expo.service;

import com.myce.expo.dto.ExpoAdminTicketResponseDto;

import java.util.List;

public interface ExpoAdminTicketService {
    List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long memberId);
    void deleteMyExpoTicket(Long memberId, Long ticketId);
}
