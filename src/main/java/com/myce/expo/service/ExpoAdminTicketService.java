package com.myce.expo.service;

import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;

import java.util.List;

public interface ExpoAdminTicketService {
    List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long memberId);
    void deleteMyExpoTicket(Long memberId, Long ticketId);
    ExpoAdminTicketResponseDto saveMyExpoTicket(Long memberId, ExpoAdminTicketRequestDto dto);
    ExpoAdminTicketResponseDto updateMyExpoTicket(Long memberId, Long ticketId, ExpoAdminTicketRequestDto dto);
}
