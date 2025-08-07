package com.myce.member.mapper;

import com.myce.member.dto.ReservedExpoResponseDto;
import com.myce.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservedExpoMapper {
    
    public List<ReservedExpoResponseDto> toResponseDtoList(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> ReservedExpoResponseDto.builder()
                        .expoId(reservation.getExpo().getId())
                        .title(reservation.getExpo().getTitle())
                        .thumbnailUrl(reservation.getExpo().getThumbnailUrl())
                        .ticketPrice(reservation.getTicket().getPrice())
                        .ticketCount(reservation.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }
}