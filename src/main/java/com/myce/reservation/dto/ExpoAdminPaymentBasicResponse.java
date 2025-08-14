package com.myce.reservation.dto;

import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExpoAdminPaymentBasicResponse {
    private Long reservationId;
    private String reservationCode;
    private String name;
    private UserType userType;
    private String loginId;
    private String phone;
    private String email;
    private Integer quantity;
    private Integer totalAmount;
    private ReservationStatus reservationStatus;
    private LocalDateTime createdAt;
}
