package com.myce.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExpoAdminPaymentResponse {
    private Long reservationId;
    private String reservationCode;
    private String name;
    private String userType;
    private String loginId;
    private String phone;
    private String email;
    private Integer quantity;
    private Integer totalAmount;
    private String reservationStatus;
    private LocalDateTime createdAt;
}
