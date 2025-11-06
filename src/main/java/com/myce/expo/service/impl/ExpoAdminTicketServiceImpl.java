package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminTicketServiceImpl implements ExpoAdminTicketService {

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final TicketRepository ticketRepository;
    private final ExpoRepository expoRepository;
    private final ExpoAdminTicketMapper mapper;
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    public List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long expoId, Long memberId, LoginType loginType) {
        expoAdminAccessValidate.ensureViewable(expoId, memberId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);
        return tickets.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMyExpoTicket(Long expoId, Long memberId, LoginType loginType, Long ticketId) {
        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expoId)){
            throw new CustomException(CustomErrorCode.TICKET_NOT_BELONG_TO_EXPO);
        }

        ensureTicketEditable(ticket);

        ticketRepository.delete(ticket);
    }

    @Override
    @Transactional
    public ExpoAdminTicketResponseDto saveMyExpoTicket(Long expoId,
                                                       Long memberId,
                                                       LoginType loginType,
                                                       ExpoAdminTicketRequestDto dto) {
        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);

        Expo expo =  getMyExpo(expoId);
        Ticket ticket = mapper.toEntity(dto,expo);
        Ticket saved = ticketRepository.save(ticket);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ExpoAdminTicketResponseDto updateMyExpoTicket(Long expoId,
                                                         Long memberId,
                                                         LoginType loginType,
                                                         Long ticketId,
                                                         ExpoAdminTicketRequestDto dto) {
        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.EXPO_DETAIL_UPDATE);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expoId)){
            throw new CustomException(CustomErrorCode.TICKET_NOT_BELONG_TO_EXPO);
        }

        ensureTicketEditable(ticket);

        ticket.updateTicketInfo(
                dto.getName(),
                dto.getDescription(),
                TicketType.fromLabel(dto.getType()),
                dto.getPrice(),
                dto.getTotalQuantity(),
                dto.getTotalQuantity(),
                dto.getSaleStartDate(),
                dto.getSaleEndDate(),
                dto.getUseStartDate(),
                dto.getUseEndDate()
        );

       return mapper.toDto(ticket);
    }

    private Expo getMyExpo(Long expoId) {
        return expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
    }

    private void ensureTicketEditable(Ticket ticket) {
        LocalDate saleStart = ticket.getSaleStartDate();

        LocalDate today = LocalDate.now(APP_ZONE);
        if (!today.isBefore(saleStart)) {
            throw new CustomException(CustomErrorCode.TICKET_EDIT_DENIED);
        }
    }
}