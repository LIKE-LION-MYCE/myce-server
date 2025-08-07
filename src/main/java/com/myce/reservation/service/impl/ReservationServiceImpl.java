package com.myce.reservation.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.mapper.ReservationDetailMapper;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}