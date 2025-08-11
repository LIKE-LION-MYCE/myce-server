package com.myce.reservation.service.Impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.service.mapper.ReservationDetailMapper;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final ReservationDetailMapper reservationDetailMapper;
    
    @Override
    public ReservationDetailResponse getReservationDetail(String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCodeWithExpoAndTicket(reservationCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        List<Reserver> reservers = reserverRepository.findByReservation(reservation);
        
        return reservationDetailMapper.toResponseDto(reservation, reservers);
    }
    
    @Override
    @Transactional
    public void updateReservers(String reservationCode, ReserverBulkUpdateRequest request) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        List<Reserver> existingReservers = reserverRepository.findByReservation(reservation);
        
        // 기존 예약자들을 ID로 매핑
        Map<Long, Reserver> reserverMap = existingReservers.stream()
                .collect(Collectors.toMap(Reserver::getId, reserver -> reserver));
        
        // 요청된 예약자 정보로 업데이트
        for (ReserverBulkUpdateRequest.ReserverInfo reserverInfo : request.getReserverInfos()) {
            Reserver reserver = reserverMap.get(reserverInfo.getReserverId());
            
            if (reserver == null) {
                throw new CustomException(CustomErrorCode.RESERVER_NOT_FOUND);
            }
            
            reserver.updateReserverInfo(
                reserverInfo.getName(),
                reserverInfo.getGender(),
                reserverInfo.getPhone(),
                reserverInfo.getEmail()
            );
        }
    }
}