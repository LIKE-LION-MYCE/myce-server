package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.entity.type.TicketType;
import com.myce.expo.repository.AdminPermissionRepository;
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
    private final AdminPermissionRepository adminPermissionRepository;

    @Override
    public List<ExpoAdminTicketResponseDto> getMyExpoTickets(Long expoId, Long memberId, LoginType loginType) {
        validateMyAccess(expoId, memberId, loginType);
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);
        return tickets.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMyExpoTicket(Long expoId, Long memberId, LoginType loginType, Long ticketId) {
        validateMyAccess(expoId, memberId, loginType);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        if(!ticket.getExpo().getId().equals(expoId)){
            throw new CustomException(CustomErrorCode.TICKET_NOT_BELONG_TO_EXPO);
        }

        ticketRepository.delete(ticket);
    }

    @Override
    @Transactional
    public ExpoAdminTicketResponseDto saveMyExpoTicket(Long expoId,
                                                       Long memberId,
                                                       LoginType loginType,
                                                       ExpoAdminTicketRequestDto dto) {
        validateMyAccess(expoId, memberId, loginType);

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
        validateMyAccess(expoId, memberId, loginType);

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

    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if(memberId == null || loginType == null){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch(loginType){
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsExpoDetailUpdateTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}