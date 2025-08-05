package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.entity.type.ExpoStatus;
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
    public List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long memberId) {
        Expo expo =  getActiveExpo(memberId);
        List<Ticket> tickets = ticketRepository.findByExpoId(expo.getId());

        return tickets.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override//TODO:하위 관리자
    @Transactional
    public void deleteMyExpoTicket(Long memberId, Long ticketId) {
        Expo expo =  getActiveExpo(memberId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expo.getId())){
            throw new CustomException(CustomErrorCode.TICKET_NOT_BELONG_TO_EXPO);
        }

        ticketRepository.delete(ticket);
    }

    @Override//TODO:하위 관리자
    @Transactional
    public void saveMyExpoTicket(Long memberId, ExpoAdminTicketRequestDto dto) {
        Expo expo =  getActiveExpo(memberId);
        Ticket ticket = mapper.toEntity(dto,expo);
        ticketRepository.save(ticket);
    }

    @Override//TODO:하위 관리자
    @Transactional
    public void updateMyExpoTicket(Long memberId, Long ticketId, ExpoAdminTicketRequestDto dto) {
        Expo expo =  getActiveExpo(memberId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expo.getId())){
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

        ticketRepository.save(ticket);
    }

    private Expo getActiveExpo(Long memberId){
        return expoRepository.findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(memberId, ExpoStatus.ACTIVE_STATUSES)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
    }
}
