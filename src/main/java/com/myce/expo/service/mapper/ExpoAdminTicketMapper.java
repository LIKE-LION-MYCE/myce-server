package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.entity.type.TicketType;
import org.springframework.stereotype.Component;

@Component
public class ExpoAdminTicketMapper {
    public ExpoAdminTicketResponseDto toDto(Ticket ticket) {
        return ExpoAdminTicketResponseDto.builder()
                .ticketId(ticket.getId())
                .name(ticket.getName())
                .type(ticket.getType().getLabel())
                .description(ticket.getDescription())
                .price(ticket.getPrice())
                .totalQuantity(ticket.getTotalQuantity())
                .saleStartDate(ticket.getSaleStartDate())
                .saleEndDate(ticket.getSaleEndDate())
                .useStartDate(ticket.getUseStartDate())
                .useEndDate(ticket.getUseEndDate())
                .build();
    }

    public Ticket toEntity(ExpoAdminTicketRequestDto dto, Expo expo) {
        return Ticket.builder()
                .expo(expo)
                .name(dto.getName())
                .description(dto.getDescription())
                .type(TicketType.fromLabel(dto.getType()))
                .price(dto.getPrice())
                .remainingQuantity(dto.getTotalQuantity())
                .totalQuantity(dto.getTotalQuantity())
                .saleStartDate(dto.getSaleStartDate())
                .saleEndDate(dto.getSaleEndDate())
                .useStartDate(dto.getUseStartDate())
                .useEndDate(dto.getUseEndDate())
                .build();
    }
}
