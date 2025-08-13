package com.myce.reservation.service.mapper;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.reservation.dto.ReservationPendingRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
  public Reservation toEntity(Expo expo, Ticket ticket, ReservationPendingRequest req, String reservationCode, ReservationStatus reservationStatus) {
    return Reservation.builder()
        .expo(expo)
        .ticket(ticket)
        .quantity(req.getQuantity())
        .userType(req.getUserType())
        .userId(req.getUserId())
        .status(reservationStatus)
        .reservationCode(reservationCode)
        .build();
  }
}
