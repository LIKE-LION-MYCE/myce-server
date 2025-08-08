package com.myce.reservation.service.Impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.reservation.dto.ReserverUpdateRequest;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.service.mapper.ReserverUpdateMapper;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ReserverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReserverServiceImpl implements ReserverService {
    
    private final ReserverRepository reserverRepository;
    private final ReserverUpdateMapper reserverUpdateMapper;
    
    @Override
    public void updateReserver(Long reserverId, ReserverUpdateRequest requestDto) {
        Reserver reserver = reserverRepository.findById(reserverId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_NOT_FOUND));
        
        reserverUpdateMapper.updateEntity(reserver, requestDto);
    }
}