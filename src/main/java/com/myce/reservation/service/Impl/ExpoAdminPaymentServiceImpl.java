package com.myce.reservation.service.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.reservation.dto.ExpoAdminPaymentBasicResponse;
import com.myce.reservation.dto.ExpoAdminPaymentDetailResponse;
import com.myce.reservation.dto.ExpoAdminPaymentResponse;
import com.myce.reservation.entity.Reserver;
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

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final ExpoAdminPaymentMapper mapper;

    @Override
    public Page<ExpoAdminPaymentResponse> getMyExpoPayments(Long expoId,
                                                            Long memberId,
                                                            LoginType loginType,
                                                            ReservationStatus status,
                                                            String name,
                                                            String phone,
                                                            Pageable pageable) {

        validateMyAccess(expoId, memberId, loginType);

        Page<ExpoAdminPaymentBasicResponse> responses =
                reservationRepository.findAllResponsesByExpoId(expoId, status, name, phone, pageable);

        return responses.map(mapper::toDto);
    }

    @Override
    public List<ExpoAdminPaymentDetailResponse> getPaymentDetail(Long expoId, Long memberId, LoginType loginType, Long paymentId) {
        validateMyAccess(expoId, memberId, loginType);

        return reserverRepository.findDetailById(paymentId);
    }

    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if(memberId == null || loginType == null){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch(loginType){
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsPaymentViewTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}