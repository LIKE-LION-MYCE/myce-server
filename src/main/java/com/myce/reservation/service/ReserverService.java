package com.myce.reservation.service;

import com.myce.reservation.dto.ReserverBulkSaveRequest;
import com.myce.reservation.dto.ReserverUpdateRequest;
import java.util.List;

public interface ReserverService {
    
    void updateReserver(Long reserverId, ReserverUpdateRequest requestDto);

    void saveReservers(Long reservationId, List<ReserverBulkSaveRequest.ReserverSaveInfo> reservers);
}