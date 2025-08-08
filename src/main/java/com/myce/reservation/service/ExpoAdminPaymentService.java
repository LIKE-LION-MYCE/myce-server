package com.myce.reservation.service;

import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.code.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpoAdminPaymentService {
    Page<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId,
                                                     Long memberId,
                                                     ReservationStatus status,
                                                     Pageable pageable);
}
