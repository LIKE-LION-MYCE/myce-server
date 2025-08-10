package com.myce.reservation.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.code.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpoAdminPaymentService {
    Page<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId,
                                                     Long memberId,
                                                     LoginType loginType,
                                                     ReservationStatus status,
                                                     String name,
                                                     String phone,
                                                     Pageable pageable);
}
