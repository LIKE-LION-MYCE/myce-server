package com.myce.reservation.service.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentDetailResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ExpoAdminPaymentService;
import com.myce.reservation.service.mapper.ExpoAdminPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminPaymentServiceImpl implements ExpoAdminPaymentService {

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final ExpoAdminPaymentMapper mapper;

    @Override
    public Page<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId,
                                                            Long memberId,
                                                            LoginType loginType,
                                                            ReservationStatus status,
                                                            String name,
                                                            String phone,
                                                            Pageable pageable) {

        expoAdminAccessValidate.ensureViewable(expoId, memberId, loginType, ExpoAdminPermission.PAYMENT_VIEW);

        Page<ExpoAdminPaymentBasicResponse> responses =
                reservationRepository.findAllResponsesByExpoId(expoId, status, name, phone, pageable);

        return responses.map(mapper::toDto);
    }

    @Override
    public List<ExpoAdminPaymentDetailResponse> getPaymentDetail(Long expoId, Long memberId, LoginType loginType, Long paymentId) {
        expoAdminAccessValidate.ensureViewable(expoId, memberId, loginType, ExpoAdminPermission.PAYMENT_VIEW);

        return reserverRepository.findDetailById(paymentId);
    }
}