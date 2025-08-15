package com.myce.reservation.service.mapper;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.ReservationPaymentSummaryResponse;
import com.myce.reservation.dto.ReservationPendingRequest;
import com.myce.reservation.dto.ReservationSuccessResponse;
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

  public ReservationSuccessResponse toSuccessResponse(Reservation reservation, String email){
    return ReservationSuccessResponse.builder()
        .reservationCode(reservation.getReservationCode())
        .email(email)
        .build();
  }

  public Reservation toPreEntity(Expo expo, Ticket ticket, PreReservationRequest req, String reservationCode, ReservationStatus reservationStatus) {
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

  public ReservationPaymentSummaryResponse toPaymentSummary(Ticket ticket, String ticketName, Integer quantity){
    return ReservationPaymentSummaryResponse.builder()
        .ticketId(ticket.getId())
        .ticketName(ticketName)
        .ticketPrice(ticket.getPrice())
        .ticketQuantity(quantity)
        .build();
  }
}
