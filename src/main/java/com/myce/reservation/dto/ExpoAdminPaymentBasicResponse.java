package com.myce.reservation.dto;

import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExpoAdminPaymentBasicResponse {
    private String reservationCode;
    private UserType userType;
    private Long userId;
    private Integer quantity;
    private Integer totalAmount;
    private LocalDateTime createdAt;
    private ReservationStatus reservationStatus;
}
