package com.myce.reservation.service.mapper;

import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentDetailResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.Reserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public ExpoAdminPaymentDetailResponse toDetailDto(Reserver reserver) {
        return ExpoAdminPaymentDetailResponse.builder()
                .name(reserver.getName())
                .gender(reserver.getGender().getLabel())
                .birth(reserver.getBirth())
                .phone(reserver.getPhone())
                .email(reserver.getEmail())
                .build();
    }
}