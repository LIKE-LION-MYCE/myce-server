package com.myce.reservation.service.mapper;

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
    
    private ReservationDetailResponse.ExpoInfo buildExpoInfo(Reservation reservation) {
        return ReservationDetailResponse.ExpoInfo.builder()
                .expoId(reservation.getExpo().getId())
                .thumbnailUrl(reservation.getExpo().getThumbnailUrl())
                .title(reservation.getExpo().getTitle())
                .location(reservation.getExpo().getLocation())
                .locationDetail(reservation.getExpo().getLocationDetail())
                .displayStartDate(reservation.getExpo().getDisplayStartDate())
                .displayEndDate(reservation.getExpo().getDisplayEndDate())
                .startTime(reservation.getExpo().getStartTime())
                .endTime(reservation.getExpo().getEndTime())
                .build();
    }
    
    private ReservationDetailResponse.ReservationInfo buildReservationInfo(Reservation reservation) {
        return ReservationDetailResponse.ReservationInfo.builder()
                .reservationCode(reservation.getReservationCode())
                .quantity(reservation.getQuantity())
                .createdAt(reservation.getCreatedAt())
                .ticketPrice(reservation.getTicket().getPrice())
                .ticketName(reservation.getTicket().getName())
                .ticketType(reservation.getTicket().getType().toString())
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
                    
                    return ReservationDetailResponse.ReserverInfo.builder()
                            .reserverId(reserver.getId())
                            .name(reserver.getName())
                            .gender(reserver.getGender())
                            .phone(reserver.getPhone())
                            .email(reserver.getEmail())
                            .qrCodeUrl(qrCodeUrl)
                            .build();
                })
                .collect(Collectors.toList());
    }
}