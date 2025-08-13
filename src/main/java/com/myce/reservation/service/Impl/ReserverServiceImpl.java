package com.myce.reservation.service.Impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.reservation.dto.ReserverBulkSaveRequest;
import com.myce.reservation.dto.ReserverUpdateRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.mapper.ReserverUpdateMapper;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ReserverService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReserverServiceImpl implements ReserverService {
    
    private final ReserverRepository reserverRepository;
    private final ReserverUpdateMapper reserverUpdateMapper;
    private final ReservationRepository reservationRepository;
    
    @Override
    public void updateReserver(Long reserverId, ReserverUpdateRequest requestDto) {
        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));
        
        reserverUpdateMapper.updateEntity(reserver, requestDto);
    }

    @Transactional
    @Override
    public void saveReservers(Long reservationId, List<ReserverBulkSaveRequest.ReserverSaveInfo> reservers) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        List<Reserver> toSave = reservers.stream()
            .map(reserver -> Reserver.builder()
                .reservation(reservation)
                .name(reserver.getName())
                .gender(reserver.getGender())
                .phone(reserver.getPhone())
                .email(reserver.getEmail())
                .birth(reserver.getBirth())
                .build())
            .toList();

        reserverRepository.saveAll(toSave);
    }
}