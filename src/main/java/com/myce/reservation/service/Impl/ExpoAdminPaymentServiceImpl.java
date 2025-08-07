package com.myce.reservation.service.Impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.ExpoRepository;
import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.ExpoAdminPaymentService;
import com.myce.reservation.service.mapper.ExpoAdminPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpoAdminPaymentServiceImpl implements ExpoAdminPaymentService {

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final ExpoAdminPaymentMapper mapper;

    @Override
    public List<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId, Long memberId) {

        if(!expoRepository.existsByIdAndMemberId(expoId, memberId)){
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        List<ExpoAdminPaymentBasicResponse> basicResponses = reservationRepository.findBasicResponsesByExpoId(expoId);

        return basicResponses.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
