package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.entity.type.TicketType;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.ExpoAdminTicketService;
import com.myce.expo.service.mapper.ExpoAdminTicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminTicketServiceImpl implements ExpoAdminTicketService {

    private final TicketRepository ticketRepository;
    private final ExpoRepository expoRepository;
    private final ExpoAdminTicketMapper mapper;

    @Override//TODO:하위 관리자
    public List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long expoId, Long memberId) {
        validateMyExpoAccess(expoId, memberId);
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);
        return tickets.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override//TODO:하위 관리자
    @Transactional
    public void deleteMyExpoTicket(Long expoId, Long memberId, Long ticketId) {
        validateMyExpoAccess(expoId, memberId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expoId)){
            throw new CustomException(CustomErrorCode.TICKET_NOT_BELONG_TO_EXPO);
        }

        ticketRepository.delete(ticket);
    }

    @Override//TODO:하위 관리자
    @Transactional
    public ExpoAdminTicketResponseDto saveMyExpoTicket(Long expoId, Long memberId, ExpoAdminTicketRequestDto dto) {
        Expo expo =  getMyExpo(expoId,memberId);
        Ticket ticket = mapper.toEntity(dto,expo);
        Ticket saved = ticketRepository.save(ticket);

        return mapper.toDto(saved);
    }

    @Override//TODO:하위 관리자
    @Transactional
    public ExpoAdminTicketResponseDto updateMyExpoTicket(Long expoId,
                                                         Long memberId,
                                                         Long ticketId,
                                                         ExpoAdminTicketRequestDto dto) {
        validateMyExpoAccess(expoId, memberId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expoId)){
            throw new CustomException(CustomErrorCode.TICKET_NOT_BELONG_TO_EXPO);
        }

        ticket.updateTicketInfo(
                dto.getName(),
                dto.getDescription(),
                TicketType.fromLabel(dto.getType()),
                dto.getPrice(),
                dto.getTotalQuantity(),
                dto.getTotalQuantity(),
                dto.getSaleStartDate(),
                dto.getSaleEndDate()
        );

       Ticket saved = ticketRepository.save(ticket);

       return mapper.toDto(saved);
    }

    private Expo getMyExpo(Long expoId, Long memberId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        if (!expo.getMember().getId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        return expo;
    }

    private void validateMyExpoAccess(Long expoId, Long memberId) {
        if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
    }
}
