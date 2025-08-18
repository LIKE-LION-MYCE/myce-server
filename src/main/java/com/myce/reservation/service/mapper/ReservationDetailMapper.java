package com.myce.reservation.service.mapper;

import com.myce.member.entity.MemberGrade;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.repository.QrCodeRepository;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationDetailMapper {
    
    private final QrCodeRepository qrCodeRepository;
    
    public ReservationDetailResponse toResponseDto(Reservation reservation, List<Reserver> reservers) {
        return ReservationDetailResponse.builder()
                .expoInfo(buildExpoInfo(reservation))
                .reservationInfo(buildReservationInfo(reservation))
                .reserverInfos(buildReserverInfos(reservers))
                .build();
    }
    
    public ReservationDetailResponse toResponseDto(Reservation reservation, List<Reserver> reservers, 
                                                  ReservationPaymentInfo paymentInfo, Payment payment, MemberGrade memberGrade) {
        return ReservationDetailResponse.builder()
                .expoInfo(buildExpoInfo(reservation))
                .reservationInfo(buildReservationInfo(reservation))
                .reserverInfos(buildReserverInfos(reservers))
                .paymentInfo(buildPaymentInfo(paymentInfo, payment, memberGrade))
                .build();
    }
    
    private ReservationDetailResponse.ExpoInfo buildExpoInfo(Reservation reservation) {
        return ReservationDetailResponse.ExpoInfo.builder()
                .expoId(reservation.getExpo().getId())
                .thumbnailUrl(reservation.getExpo().getThumbnailUrl())
                .title(reservation.getExpo().getTitle())
                .location(reservation.getExpo().getLocation())
                .locationDetail(reservation.getExpo().getLocationDetail())
                .startDate(reservation.getExpo().getStartDate())
                .endDate(reservation.getExpo().getEndDate())
                .displayStartDate(reservation.getExpo().getDisplayStartDate())
                .displayEndDate(reservation.getExpo().getDisplayEndDate())
                .startTime(reservation.getExpo().getStartTime())
                .endTime(reservation.getExpo().getEndTime())
                .build();
    }
    
    private ReservationDetailResponse.ReservationInfo buildReservationInfo(Reservation reservation) {
        return ReservationDetailResponse.ReservationInfo.builder()
                .reservationId(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .status(reservation.getStatus().name())
                .quantity(reservation.getQuantity())
                .createdAt(reservation.getCreatedAt())
                .ticketPrice(reservation.getTicket().getPrice())
                .ticketName(reservation.getTicket().getName())
                .ticketType(reservation.getTicket().getType().toString())
                .ticketUseStartDate(reservation.getTicket().getUseStartDate())
                .ticketUseEndDate(reservation.getTicket().getUseEndDate())
                .build();
    }
    
    private List<ReservationDetailResponse.ReserverInfo> buildReserverInfos(List<Reserver> reservers) {
        Map<Long, Optional<QrCode>> qrCodeMap = reservers.stream()
                .collect(Collectors.toMap(
                        Reserver::getId,
                        reserver -> qrCodeRepository.findByReserverId(reserver.getId())
                ));
        
        return reservers.stream()
                .map(reserver -> {
                    Optional<QrCode> qrCodeOpt = qrCodeMap.get(reserver.getId());
                    String qrCodeUrl = qrCodeOpt
                            .map(QrCode::getQrImageUrl)
                            .orElse(null);
                    String qrStatus = qrCodeOpt
                            .map(qrCode -> qrCode.getStatus().name())
                            .orElse("NOT_ISSUED");
                    
                    return ReservationDetailResponse.ReserverInfo.builder()
                            .reserverId(reserver.getId())
                            .name(reserver.getName())
                            .gender(reserver.getGender())
                            .phone(reserver.getPhone())
                            .email(reserver.getEmail())
                            .qrCodeUrl(qrCodeUrl)
                            .qrStatus(qrStatus)
                            .qrUsedAt(qrCodeOpt.map(QrCode::getUsedAt).orElse(null))
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    private ReservationDetailResponse.PaymentInfo buildPaymentInfo(ReservationPaymentInfo paymentInfo, 
                                                                   Payment payment, MemberGrade memberGrade) {
        if (paymentInfo == null) {
            return null;
        }
        
        String paymentMethod = payment != null ? payment.getPaymentMethod().name() : null;
        String paymentDetail = null;
        
        if (payment != null) {
            // 카드 결제인 경우
            if (payment.getCardCompany() != null && payment.getCardNumber() != null) {
                paymentDetail = payment.getCardCompany() + " " + maskCardNumber(payment.getCardNumber());
            }
            // 계좌 이체인 경우
            else if (payment.getAccountBank() != null && payment.getAccountNumber() != null) {
                paymentDetail = payment.getAccountBank() + " " + maskAccountNumber(payment.getAccountNumber());
            }
        }
        
        return ReservationDetailResponse.PaymentInfo.builder()
                .usedMileage(paymentInfo.getUsedMileage())
                .savedMileage(paymentInfo.getSavedMileage())
                .totalAmount(paymentInfo.getTotalAmount())
                .paymentStatus(paymentInfo.getStatus().name())
                .paymentMethod(paymentMethod)
                .paymentDetail(paymentDetail)
                .paidAt(payment != null ? payment.getPaidAt() : null)
                .memberGrade(memberGrade != null ? memberGrade.getDescription() : null)
                .mileageRate(memberGrade != null ? memberGrade.getMileageRate() : null)
                .gradeDescription(memberGrade != null ? memberGrade.getDescription() : null)
                .build();
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}