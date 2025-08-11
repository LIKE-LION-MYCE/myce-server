package com.myce.member.mapper;

import com.myce.expo.entity.Expo;
import com.myce.member.dto.PaymentHistoryResponse;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.reservation.entity.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentHistoryMapper {
    
    public List<PaymentHistoryResponse> toResponseDtoList(List<Object[]> paymentHistoryData) {
        return paymentHistoryData.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
    
    public PaymentHistoryResponse toResponseDto(Object[] data) {
        Payment payment = (Payment) data[0];
        ReservationPaymentInfo paymentInfo = (ReservationPaymentInfo) data[1];
        Reservation reservation = (Reservation) data[2];
        Expo expo = (Expo) data[3];
        
        return PaymentHistoryResponse.builder()
                .paymentNumber(payment.getImpUid())
                .paymentDate(payment.getCreatedAt())
                .expoTitle(expo.getTitle())
                .totalAmount(paymentInfo.getTotalAmount())
                .status(paymentInfo.getStatus())
                .reservationCode(reservation.getReservationCode())
                .build();
    }
}