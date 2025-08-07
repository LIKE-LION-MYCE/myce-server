package com.myce.reservation.service;

import com.myce.reservation.dto.ExpoAdminPaymentResponse;

import java.util.List;

public interface ExpoAdminPaymentService {
    List<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId, Long memberId);
}
