package com.myce.expo.service;

import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;

import java.util.List;

public interface ExpoAdminTicketService {
    List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long expoId, Long memberId);
    void deleteMyExpoTicket(Long expoId, Long memberId, Long ticketId);
    ExpoAdminTicketResponseDto saveMyExpoTicket(Long expoId, Long memberId, ExpoAdminTicketRequestDto dto);
    ExpoAdminTicketResponseDto updateMyExpoTicket(Long expoId, Long memberId, Long ticketId, ExpoAdminTicketRequestDto dto);
}
