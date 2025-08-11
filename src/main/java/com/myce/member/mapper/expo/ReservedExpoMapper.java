package com.myce.member.mapper.expo;

import com.myce.member.dto.expo.ReservedExpoResponse;
import com.myce.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservedExpoMapper {
    
    public List<ReservedExpoResponse> toResponseDtoList(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> ReservedExpoResponse.builder()
                        .expoId(reservation.getExpo().getId())
                        .title(reservation.getExpo().getTitle())
                        .thumbnailUrl(reservation.getExpo().getThumbnailUrl())
                        .ticketPrice(reservation.getTicket().getPrice())
                        .ticketCount(reservation.getQuantity())
                        .reservationCode(reservation.getReservationCode())
                        .build())
                .collect(Collectors.toList());
    }
}