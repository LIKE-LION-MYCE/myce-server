package com.myce.reservation.service.mapper;

import com.myce.reservation.dto.ReserverUpdateRequest;
import com.myce.reservation.entity.Reserver;
import org.springframework.stereotype.Component;

@Component
public class ReserverUpdateMapper {
    
    public void updateEntity(Reserver reserver, ReserverUpdateRequest requestDto) {
        reserver.updateReserverInfo(
                requestDto.getName(),
                requestDto.getGender(),
                requestDto.getPhone(),
                requestDto.getEmail()
        );
    }
}