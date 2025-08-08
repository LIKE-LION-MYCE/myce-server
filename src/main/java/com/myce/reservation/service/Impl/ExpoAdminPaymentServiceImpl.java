package com.myce.reservation.service.Impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.ExpoRepository;
import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.ExpoAdminPaymentService;
import com.myce.reservation.service.mapper.ExpoAdminPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpoAdminPaymentServiceImpl implements ExpoAdminPaymentService {

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final ExpoAdminPaymentMapper mapper;

    @Override//TODO:하위관리자
    public Page<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId,
                                                            Long memberId,
                                                            ReservationStatus status,
                                                            Pageable pageable) {

        if(!expoRepository.existsByIdAndMemberId(expoId, memberId)){
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        Page<ExpoAdminPaymentBasicResponse> basicResponses =
                reservationRepository.findBasicResponsesByExpoId(expoId, status, pageable);

        return basicResponses.map(mapper::toDto);
    }
}
