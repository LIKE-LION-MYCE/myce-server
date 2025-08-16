package com.myce.member.mapper.expo;

import com.myce.member.dto.expo.ReservedExpoResponse;
import com.myce.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservedExpoMapper {
    
    public ReservedExpoResponse toResponseDto(Reservation reservation) {
        return ReservedExpoResponse.builder()
                .expoId(reservation.getExpo().getId())
                .title(reservation.getExpo().getTitle())
                .thumbnailUrl(reservation.getExpo().getThumbnailUrl())
                .ticketPrice(reservation.getTicket().getPrice())
                .ticketCount(reservation.getQuantity())
                .ticketName(reservation.getTicket().getName())
                .reservationId(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .createdAt(reservation.getCreatedAt())
                .reservationStatus(reservation.getStatus())
                .build();
    }
    
    public List<ReservedExpoResponse> toResponseDtoList(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}