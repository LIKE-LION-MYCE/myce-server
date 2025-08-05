package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.entity.Ticket;
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
                .build();
    }
}
