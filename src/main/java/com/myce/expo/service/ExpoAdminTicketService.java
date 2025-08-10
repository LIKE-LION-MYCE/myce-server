package com.myce.expo.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;

import java.util.List;

public interface ExpoAdminTicketService {
    List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long expoId, Long memberId, LoginType loginType);

    void deleteMyExpoTicket(Long expoId, Long memberId, LoginType loginType, Long ticketId);

    ExpoAdminTicketResponseDto saveMyExpoTicket(Long expoId,
                                                Long memberId,
                                                LoginType loginType,
                                                ExpoAdminTicketRequestDto dto);
    ExpoAdminTicketResponseDto updateMyExpoTicket(Long expoId,
                                                  Long memberId,
                                                  LoginType loginType,
                                                  Long ticketId,
                                                  ExpoAdminTicketRequestDto dto);
}
