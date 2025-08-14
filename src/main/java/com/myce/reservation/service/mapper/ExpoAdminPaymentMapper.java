package com.myce.reservation.service.mapper;

import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpoAdminPaymentMapper {

    public ExpoAdminPaymentResponse toDto(ExpoAdminPaymentBasicResponse response) {
        return ExpoAdminPaymentResponse.builder()
                .reservationId(response.getReservationId())
                .reservationCode(response.getReservationCode())
                .name(response.getName())
                .userType(response.getUserType().getLabel())
                .loginId(response.getLoginId())
                .phone(response.getPhone())
                .email(response.getEmail())
                .quantity(response.getQuantity())
                .totalAmount(response.getTotalAmount())
                .reservationStatus(response.getReservationStatus().getLabel())
                .createdAt(response.getCreatedAt())
                .build();
    }
}