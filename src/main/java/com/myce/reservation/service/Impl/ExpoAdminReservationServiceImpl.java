package com.myce.reservation.service.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ExpoAdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminReservationServiceImpl implements ExpoAdminReservationService {

    private final ExpoRepository expoRepository;
    private final TicketRepository ticketRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final ReserverRepository reserverRepository;

    @Override
    public List<String> getExpoTicketNames(Long expoId, Long memberId, LoginType loginType) {
        validateMyAccess(expoId,memberId,loginType);
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);

        return tickets.stream().map(Ticket::getName).toList();
    }

    @Override
    public Page<ExpoAdminReservationResponse> getMyExpoReservations(
            Long expoId, Long memberId, LoginType loginType,
            String entranceStatus, String name, String phone, String reservationCode, String ticketName,
            Pageable pageable) {

        validateMyAccess(expoId, memberId, loginType);

        return reserverRepository.findAllResponsesByExpoIdAndStatus(
                expoId, entranceStatus, name, phone, reservationCode, ticketName, pageable);
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
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsReserverListViewTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}