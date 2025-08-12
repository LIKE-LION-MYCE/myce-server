package com.myce.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpoAdminReservationResponse {
    private Long reserverId;
    private String reservationCode;
    private String name;
    private String gender;
    private LocalDate birth;
    private String phone;
    private String email;
    private String ticketName;
    private LocalDateTime entranceAt;
    private String entranceStatus;
}