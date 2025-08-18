package com.myce.reservation.dto;

import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreReservationCacheDto {
    
    private Long expoId;
    private Long ticketId;
    private String reservationCode;
    private UserType userType;
    private Long userId;
    private Integer quantity;
    private ReservationStatus status;
}