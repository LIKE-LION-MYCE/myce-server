package com.myce.expo.service.impl;

import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.ExpoAdminTicketRepository;
import com.myce.expo.service.ExpoAdminTicketService;
import com.myce.expo.service.mapper.ExpoAdminTicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminTicketServiceImpl implements ExpoAdminTicketService {

    private final ExpoAdminTicketRepository repository;
    private final ExpoAdminTicketMapper mapper;

    @Override
    public List<ExpoAdminTicketResponseDto> getMyExpoTickets() {
        List<Ticket> tickets = repository.findByExpoId(1L); //TODO : 현재 로그인 한 관리자가 속한 expoId를 기준으로 교체

        return tickets.stream()
                .map(mapper::toDto)
                .toList();
    }
}
